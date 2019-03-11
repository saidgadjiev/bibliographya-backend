package ru.saidgadjiev.bibliographya.service.impl.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.domain.RequestResult;
import ru.saidgadjiev.bibliographya.domain.SignUpResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.SocialService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.SessionEmailVerificationService;
import ru.saidgadjiev.bibliographya.service.impl.SessionManager;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Locale;
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

    private UIProperties uiProperties;

    private SessionManager sessionManager;

    private ObjectMapper objectMapper;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
            (request, response, authentication) -> CookieUtils.deleteCookie(response, uiProperties.getName(), "X-TOKEN"),
            new SecurityContextLogoutHandler()
    );

    @Autowired
    public AuthService(SocialServiceFactory socialServiceFactory,
                       BibliographyaUserDetailsService userAccountDetailsService,
                       TokenService tokenService,
                       SecurityService securityService,
                       SessionEmailVerificationService emailVerificationService,
                       UIProperties uiProperties,
                       SessionManager sessionManager,
                       ObjectMapper objectMapper) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.emailVerificationService = emailVerificationService;
        this.uiProperties = uiProperties;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
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
                                authContext.getSignUpRequest().getEmail(),
                                authContext.getSignUpRequest().getPassword()
                        );
                Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                user = (User) authentication.getPrincipal();
                break;
        }

        auth(authContext.getResponse(), user, accessGrant);

        return user;
    }

    public HttpStatus signUp(HttpServletRequest request, Locale locale, SignUpRequest signUpRequest) throws MessagingException {
        if (userAccountDetailsService.isExistEmail(signUpRequest.getEmail())) {
            return HttpStatus.CONFLICT;
        }
        sessionManager.setSignUp(request, signUpRequest);

        emailVerificationService.sendVerification(request, locale, signUpRequest.getEmail());

        return HttpStatus.OK;
    }

    public SignUpResult confirmSignUp(AuthContext authContext, Integer code) throws SQLException {
        SignUpRequest signUpRequest = sessionManager.getSignUp(authContext.getRequest());

        if (signUpRequest != null) {
            EmailVerificationResult result = emailVerificationService.verify(authContext.getRequest(), signUpRequest.getEmail(), code);

            if (result.isValid()) {
                User user = userAccountDetailsService.save(signUpRequest);

                user.setIsNew(true);
                sessionManager.removeState(authContext.getRequest());

                auth(authContext.getResponse(), user, null);

                return new SignUpResult().setStatus(HttpStatus.OK).setUser(user);
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

    public RequestResult<?> account() {
        UserDetails userDetails = securityService.findLoggedInUser();

        if (userDetails == null) {
            return new RequestResult().setStatus(HttpStatus.NOT_FOUND);
        }

        return new RequestResult<UserDetails>().setStatus(HttpStatus.OK).setBody(userDetails);
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
                    Integer accountId = (Integer) details.get("accountId");

                    userDetails = userAccountDetailsService.loadUserAccountById(accountId);
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

    public void cancelSignUp(HttpServletRequest request) {
        sessionManager.removeState(request);
    }

    private void auth(HttpServletResponse response, User user, AccessGrant accessGrant) {
        securityService.authenticate(user);

        String token = tokenService.createToken(user, accessGrant);

        CookieUtils.addCookie(response, uiProperties.getName(), "X-TOKEN", token);

    }

    public RequestResult<?> confirmation(HttpServletRequest request) {
        SignUpRequest signUpRequest = sessionManager.getSignUp(request);

        if (signUpRequest == null) {
            return new RequestResult<ObjectNode>().setStatus(HttpStatus.FOUND);
        }
        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("email", signUpRequest.getEmail());

        return new RequestResult<ObjectNode>().setStatus(HttpStatus.OK).setBody(objectNode);
    }
}
