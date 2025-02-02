package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.ResponseType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.service.api.*;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by said on 24.12.2018.
 */
@Service
public class AuthService {

    private final SocialServiceFactory socialServiceFactory;

    private BibliographyaUserDetailsService userAccountDetailsService;

    private AuthTokenService tokenService;

    private SecurityService securityService;

    private VerificationService verificationService;

    private VerificationStorage verificationStorage;

    private JwtProperties jwtProperties;

    private BruteForceService bruteForceService;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public AuthService(SocialServiceFactory socialServiceFactory,
                       BibliographyaUserDetailsService userAccountDetailsService,
                       AuthTokenService tokenService,
                       SecurityService securityService,
                       @Qualifier("wrapper") VerificationService verificationService,
                       @Qualifier("inMemory") VerificationStorage verificationStorage,
                       JwtProperties jwtProperties,
                       BruteForceService bruteForceService,
                       ApplicationEventPublisher eventPublisher) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.verificationService = verificationService;
        this.verificationStorage = verificationStorage;
        this.jwtProperties = jwtProperties;
        this.bruteForceService = bruteForceService;
        this.eventPublisher = eventPublisher;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri, ResponseType responseType) {
        SocialService socialService = socialServiceFactory.getService(providerType);

        if (socialService == null) {
            return null;
        }

        return socialService.createOAuth2Url(redirectUri, responseType);
    }

    public HttpStatus signUp(AuthContext authContext) {
        SignUpRequest signUpRequest = null;

        switch (authContext.getProviderType()) {
            case FACEBOOK:
            case VK:
                SocialService socialService = socialServiceFactory.getService(authContext.getProviderType());
                AccessGrant accessGrant = socialService.createAccessToken(authContext.getCode(), authContext.getRedirectUri());

                SocialUserInfo userInfo = socialService.getUserInfo(accessGrant.getUserId(), accessGrant.getAccessToken());

                signUpRequest = new SignUpRequest();

                signUpRequest.setFirstName(userInfo.getFirstName());
                signUpRequest.setLastName(userInfo.getLastName());
                signUpRequest.setMiddleName(userInfo.getMiddleName());

                break;
            case SIMPLE:
                signUpRequest = (SignUpRequest) authContext.getBody();

                break;
        }

        verificationStorage.expire(authContext.getRequest());
        verificationStorage.setAttr(authContext.getRequest(), VerificationStorage.STATE, SessionState.SIGN_UP_CONFIRM);
        verificationStorage.setAttr(authContext.getRequest(), VerificationStorage.SIGN_UP_REQUEST, signUpRequest);

        return HttpStatus.OK;
    }

    public SignInResult confirmSignUpFinish(AuthContext authContext) throws SQLException {
        SignUpRequest signUpRequest = (SignUpRequest) verificationStorage.getAttr(authContext.getRequest(), VerificationStorage.SIGN_UP_REQUEST);
        SignUpConfirmation signUpConfirmation = (SignUpConfirmation) authContext.getBody();

        if (signUpRequest != null) {
            VerificationResult result = verificationService.verify(
                    authContext.getRequest(),
                    signUpConfirmation.getCode(),
                    true
            );

            if (result.isValid()) {
                AuthKey authKey = (AuthKey) verificationStorage.getAttr(authContext.getRequest(), VerificationStorage.AUTH_KEY, null);

                if (authKey == null) {
                    return new SignInResult().setStatus(HttpStatus.BAD_REQUEST);
                }

                User saveUser = new User();
                UserAccount userAccount = new UserAccount();

                userAccount.setPhone(authKey.formattedNumber());
                userAccount.setPassword(signUpConfirmation.getPassword());

                saveUser.setUserAccount(userAccount);
                saveUser.setProviderType(ProviderType.SIMPLE);

                Biography biography = new Biography();

                biography.setFirstName(signUpRequest.getFirstName());
                biography.setLastName(signUpRequest.getLastName());
                biography.setMiddleName(signUpRequest.getMiddleName());

                saveUser.setBiography(biography);

                User user = userAccountDetailsService.save(saveUser);

                user.setIsNew(true);

                verificationStorage.expire(authContext.getRequest());

                securityService.autoLogin(user);

                String token = tokenService.createToken(user);

                //CookieUtils.addCookie(authContext.getResponse(), uiProperties.getHost(), jwtProperties.tokenName(), token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        saveUser.getAuthorities()
                );

                eventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

                authContext.getResponse().addHeader(jwtProperties.tokenName(), token);

                return new SignInResult().setStatus(HttpStatus.OK).setUser(user);
            }

            return new SignInResult().setStatus(HttpStatus.PRECONDITION_FAILED);
        }

        return new SignInResult().setStatus(HttpStatus.BAD_REQUEST);
    }

    public SendVerificationResult confirmSignUpStart(HttpServletRequest request, Locale locale, AuthKey authKey) throws MessagingException {
        if (userAccountDetailsService.isExist(authKey)) {
            return new SendVerificationResult(HttpStatus.CONFLICT, null, null);
        }

        if (bruteForceService.isBlocked(request, BruteForceService.Type.SIGN_UP)) {
            return new SendVerificationResult(HttpStatus.TOO_MANY_REQUESTS, null, null);
        }
        bruteForceService.count(request, BruteForceService.Type.SIGN_UP);

        return verificationService.sendVerification(request, locale, authKey);
    }

    public void cancelSignUp(HttpServletRequest request) {
        verificationStorage.expire(request);
    }

    public HttpStatus confirmation(HttpServletRequest request) {
        SignUpRequest signUpRequest = (SignUpRequest) verificationStorage.getAttr(request, VerificationStorage.SIGN_UP_REQUEST);

        if (signUpRequest == null) {
            return HttpStatus.FOUND;
        }

        return HttpStatus.OK;
    }
}
