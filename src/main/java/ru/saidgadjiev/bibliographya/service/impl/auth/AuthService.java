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
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.EmailVerificationService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;
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

    public static final String SESSION_SIGNING_UP = "signingUp";

    private FacebookService facebookService;

    private VKService vkService;

    private BibliographyaUserDetailsService userAccountDetailsService;

    private TokenService tokenService;

    private SecurityService securityService;

    private AuthenticationManager authenticationManager;

    private EmailVerificationService emailVerificationService;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
            (request, response, authentication) -> CookieUtils.deleteCookie(request, response,"X-TOKEN"),
            new SecurityContextLogoutHandler()
    );

    @Autowired
    public void setEmailVerificationService(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @Autowired
    public void setFacebookService(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @Autowired
    public void setVkService(VKService vkService) {
        this.vkService = vkService;
    }

    @Autowired
    public void setUserAccountDetailsService(BibliographyaUserDetailsService userAccountDetailsService) {
        this.userAccountDetailsService = userAccountDetailsService;
    }

    @Autowired
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri) {
        switch (providerType) {
            case FACEBOOK:
                return facebookService.createFacebookAuthorizationUrl(redirectUri);
            case VK:
                return vkService.createVKAuthorizationUrl(redirectUri);
            case EMAIL_PASSWORD:
                break;
        }

        return null;
    }

    public User auth(AuthContext authContext, String redirectUri) throws SQLException {
        User user = null;
        AccessGrant accessGrant = null;

        switch (authContext.getProviderType()) {
            case FACEBOOK: {
                accessGrant = facebookService.createFacebookAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = facebookService.getUserInfo(accessGrant.getAccessToken());

                user = userAccountDetailsService.loadSocialUserByAccountId(ProviderType.FACEBOOK, userInfo.getId());

                if (user == null) {
                    user = userAccountDetailsService.saveSocialUser(userInfo);

                    user.setIsNew(true);
                }

                break;
            }
            case VK: {
                accessGrant = vkService.createAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = vkService.getUserInfo(accessGrant.getUserId(), accessGrant.getAccessToken());

                user = userAccountDetailsService.loadSocialUserByAccountId(ProviderType.VK, userInfo.getId());

                if (user == null) {
                    user = userAccountDetailsService.saveSocialUser(userInfo);

                    user.setIsNew(true);
                }

                break;
            }
            case EMAIL_PASSWORD:
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                authContext.getSignInRequest().getUsername(),
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

    public SignUpResult signUp(HttpServletRequest request, SignUpRequest signUpRequest) {
        HttpSession session = request.getSession(true);

        session.setAttribute("signingUp", true);
        session.setAttribute("request", signUpRequest);

        emailVerificationService.sendVerification(signUpRequest.getEmail());

        return null;
    }

    public SignUpResult confirmSignUp(HttpServletRequest request, Integer code) throws SQLException {
        HttpSession session = request.getSession(false);

        if (isSigningUp(session)) {
            SignUpRequest signUpRequest = (SignUpRequest) session.getAttribute("request");

            EmailVerificationResult result = emailVerificationService.confirm(signUpRequest.getEmail(), code);

            if (result.isValid()) {
                session.invalidate();
                userAccountDetailsService.save(signUpRequest);

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

    public AccountResult account(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (isSigningUp(session)) {
            return new AccountResult().setStatus(HttpStatus.PRECONDITION_REQUIRED);
        } else {
            UserDetails userDetails = securityService.findLoggedInUser();

            if (userDetails == null) {
                return new AccountResult().setStatus(HttpStatus.NOT_FOUND);
            }

            return new AccountResult().setStatus(HttpStatus.OK).setAccount((User) userDetails);
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
        Boolean signingUp = (Boolean) session.getAttribute(SESSION_SIGNING_UP);

        return signingUp != null && signingUp;
    }
}
