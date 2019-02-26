package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.AccountResult;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.domain.SignUpResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.SocialService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.SessionEmailVerificationService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by said on 24.12.2018.
 */
@Service
public class AuthService {

    private final SocialServiceFactory socialServiceFactory;

    private BibliographyaUserDetailsService userAccountDetailsService;

    private TokenService tokenService;

    private SecurityService securityService;

    private AuthenticationManager authenticationManager;

    private SessionEmailVerificationService emailVerificationService;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
            (request, response, authentication) -> CookieUtils.deleteCookie(request, response,"X-TOKEN"),
            new SecurityContextLogoutHandler()
    );

    @Autowired
    public AuthService(SocialServiceFactory socialServiceFactory,
                       BibliographyaUserDetailsService userAccountDetailsService,
                       TokenService tokenService,
                       SecurityService securityService,
                       SessionEmailVerificationService emailVerificationService) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.emailVerificationService = emailVerificationService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri) {
        SocialService socialService = socialServiceFactory.getService(providerType);

        if (socialService == null) {
            return null;
        }

        return socialService.createOAuth2Url(redirectUri);
    }

    public User auth(AuthContext authContext, String redirectUri) throws SQLException {
        User user = null;
        AccessGrant accessGrant = null;

        SocialService socialService = socialServiceFactory.getService(authContext.getProviderType());

        switch (authContext.getProviderType()) {
            case VK:
            case FACEBOOK: {
                accessGrant = socialService.createAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = socialService.getUserInfo(null, accessGrant.getAccessToken());

                user = userAccountDetailsService.loadSocialUserByAccountId(authContext.getProviderType(), userInfo.getId());

                if (user == null) {
                    user = userAccountDetailsService.saveSocialUser(userInfo);

                    user.setIsNew(true);
                }

                break;
            }
            case EMAIL_PASSWORD:
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                authContext.getSignInRequest().getEmail(),
                                authContext.getSignInRequest().getPassword()
                        );
                Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                user = (User) authentication.getPrincipal();

                break;
        }

        securityService.authenticate(user);

        String token = tokenService.createToken(user, accessGrant);

        CookieUtils.addCookie(authContext.getRequest(), authContext.getResponse(), "X-TOKEN", token);

        return user;
    }

    public HttpStatus signUp(HttpServletRequest request, SignUpRequest signUpRequest) {
        if (userAccountDetailsService.isExistEmail(signUpRequest.getEmail())) {
            return HttpStatus.CONFLICT;
        }
        HttpSession session = request.getSession(true);

        session.setAttribute("state", SessionState.SIGN_UP_CONFIRM);
        session.setAttribute("request", signUpRequest);

        emailVerificationService.sendVerification(request, signUpRequest.getEmail());

        return HttpStatus.OK;
    }

    public SignUpResult confirmSignUp(HttpServletRequest request, Integer code) throws SQLException {
        HttpSession session = request.getSession(false);

        if (isSigningUp(session)) {
            SignUpRequest signUpRequest = (SignUpRequest) session.getAttribute("request");

            EmailVerificationResult result = emailVerificationService.confirm(request, signUpRequest.getEmail(), code);

            if (result.isValid()) {
                userAccountDetailsService.save(signUpRequest);
                session.removeAttribute("state");
                session.removeAttribute("request");

                return new SignUpResult().setStatus(HttpStatus.OK);
            }

            return new SignUpResult().setStatus(HttpStatus.PRECONDITION_FAILED);
        }

        return new SignUpResult().setStatus(HttpStatus.BAD_REQUEST);
    }

    public User signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = securityService.findLoggedInUserAuthentication();

        if (authentication != null) {
            User user = (User) authentication.getPrincipal();

            logoutHandler.logout(request, response, authentication);

            return user;
        }

        return null;
    }

    public AccountResult<?> account(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (isSigningUp(session)) {
            SignUpRequest signUpRequest = (SignUpRequest) session.getAttribute("request");
            SignUpRequest result = new SignUpRequest();

            result.setEmail(signUpRequest.getEmail());

            return new AccountResult<SignUpRequest>().setBody(result).setStatus(HttpStatus.PRECONDITION_REQUIRED);
        } else {
            UserDetails userDetails = securityService.findLoggedInUser();

            if (userDetails == null) {
                return new AccountResult().setStatus(HttpStatus.NOT_FOUND);
            }

            return new AccountResult<UserDetails>().setStatus(HttpStatus.OK).setBody(userDetails);
        }
    }

    public void tokenAuth(String token) {
        Map<String, Object> details = tokenService.validate(token);

        if (details == null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        } else {
            ProviderType providerType = ProviderType.fromId((String) details.get("providerId"));
            Integer userId = (Integer) details.get("userId");
            UserDetails userDetails = null;

            switch (providerType) {
                case VK:
                case FACEBOOK:
                    userDetails = userAccountDetailsService.loadSocialUserById(userId);
                    break;
                case EMAIL_PASSWORD:
                    userDetails = userAccountDetailsService.loadUserById(userId);
                    break;
            }

            if (userDetails != null) {
                ((CredentialsContainer) userDetails).eraseCredentials();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }
    }

    private boolean isSigningUp(HttpSession session) {
        if (session == null) {
            return false;
        }
        SessionState sessionState = (SessionState) session.getAttribute("state");

        return sessionState != null && sessionState.equals(SessionState.SIGN_UP_CONFIRM);
    }

    public void cancelSignUp(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}
