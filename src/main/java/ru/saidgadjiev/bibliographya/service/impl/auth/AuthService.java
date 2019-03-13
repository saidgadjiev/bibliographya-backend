package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.SocialService;
import ru.saidgadjiev.bibliographya.service.impl.HttpSessionEmailVerificationService;
import ru.saidgadjiev.bibliographya.service.impl.HttpSessionManager;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by said on 24.12.2018.
 */
@Service
public class AuthService {

    private final SocialServiceFactory socialServiceFactory;

    private BibliographyaUserDetailsService userAccountDetailsService;

    private TokenService tokenService;

    private SecurityService securityService;

    private HttpSessionEmailVerificationService emailVerificationService;

    private UIProperties uiProperties;

    private HttpSessionManager httpSessionManager;

    private JwtProperties jwtProperties;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public AuthService(SocialServiceFactory socialServiceFactory,
                       BibliographyaUserDetailsService userAccountDetailsService,
                       TokenService tokenService,
                       SecurityService securityService,
                       HttpSessionEmailVerificationService emailVerificationService,
                       UIProperties uiProperties,
                       HttpSessionManager httpSessionManager,
                       JwtProperties jwtProperties,
                       ApplicationEventPublisher eventPublisher) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.emailVerificationService = emailVerificationService;
        this.uiProperties = uiProperties;
        this.httpSessionManager = httpSessionManager;
        this.jwtProperties = jwtProperties;
        this.eventPublisher = eventPublisher;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri) {
        SocialService socialService = socialServiceFactory.getService(providerType);

        if (socialService == null) {
            return null;
        }

        return socialService.createOAuth2Url(redirectUri);
    }

    public HttpStatus signUp(AuthContext authContext, String redirectUri) {
        SignUpRequest signUpRequest = null;
        SocialService socialService = socialServiceFactory.getService(authContext.getProviderType());

        switch (authContext.getProviderType()) {
            case FACEBOOK:
            case VK:
                AccessGrant accessGrant = socialService.createAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = socialService.getUserInfo(null, accessGrant.getAccessToken());

                signUpRequest = new SignUpRequest();

                signUpRequest.setFirstName(userInfo.getFirstName());
                signUpRequest.setLastName(userInfo.getLastName());
                signUpRequest.setMiddleName(userInfo.getMiddleName());

                break;
            case EMAIL_PASSWORD:
                signUpRequest = (SignUpRequest) authContext.getBody();

                break;
        }

        httpSessionManager.setSignUp(authContext.getRequest(), signUpRequest);

        return HttpStatus.OK;
    }

    public SignUpResult confirmSignUpFinish(AuthContext authContext) throws SQLException {
        SignUpRequest signUpRequest = httpSessionManager.getSignUp(authContext.getRequest());
        SignUpConfirmation signUpConfirmation = (SignUpConfirmation) authContext.getBody();

        if (signUpRequest != null) {
            EmailVerificationResult result = emailVerificationService.verify(
                    authContext.getRequest(),
                    signUpConfirmation.getEmail(),
                    signUpConfirmation.getCode()
            );

            if (result.isValid()) {
                User saveUser = new User();

                saveUser.setEmail(signUpConfirmation.getEmail());
                saveUser.setPassword(signUpConfirmation.getPassword());

                Biography biography = new Biography();

                biography.setFirstName(signUpRequest.getFirstName());
                biography.setLastName(signUpRequest.getLastName());
                biography.setMiddleName(signUpRequest.getMiddleName());

                saveUser.setBiography(biography);

                User user = userAccountDetailsService.save(saveUser);

                user.setIsNew(true);

                httpSessionManager.removeState(authContext.getRequest());

                Authentication authentication = securityService.autoLogin(user);

                String token = tokenService.createToken(user);

                CookieUtils.addCookie(
                        authContext.getResponse(),
                        uiProperties.getName(),
                        jwtProperties.cookieName(),
                        token
                );

                eventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

                return new SignUpResult().setStatus(HttpStatus.OK).setUser(user);
            }

            return new SignUpResult().setStatus(HttpStatus.PRECONDITION_FAILED);
        }

        return new SignUpResult().setStatus(HttpStatus.BAD_REQUEST);
    }

    public HttpStatus confirmSignUpStart(HttpServletRequest request, Locale locale, String email) throws MessagingException {
        if (userAccountDetailsService.isExistEmail(email)) {
            return HttpStatus.CONFLICT;
        }

        emailVerificationService.sendVerification(request, locale, email);

        return HttpStatus.OK;
    }

    public void cancelSignUp(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        httpSessionManager.removeState(request);
    }

    public HttpStatus confirmation(HttpServletRequest request) {
        SignUpRequest signUpRequest = httpSessionManager.getSignUp(request);

        if (signUpRequest == null) {
            return HttpStatus.FOUND;
        }

        return HttpStatus.OK;
    }
}
