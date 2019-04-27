package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.SocialService;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

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

    private UIProperties uiProperties;

    private VerificationStorage verificationStorage;

    private JwtProperties jwtProperties;

    @Autowired
    public AuthService(SocialServiceFactory socialServiceFactory,
                       BibliographyaUserDetailsService userAccountDetailsService,
                       AuthTokenService tokenService,
                       SecurityService securityService,
                       @Qualifier("wrapper") VerificationService verificationService,
                       UIProperties uiProperties,
                       @Qualifier("inMemory") VerificationStorage verificationStorage,
                       JwtProperties jwtProperties) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.verificationService = verificationService;
        this.uiProperties = uiProperties;
        this.verificationStorage = verificationStorage;
        this.jwtProperties = jwtProperties;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri, ResponseType responseType) {
        SocialService socialService = socialServiceFactory.getService(providerType);

        if (socialService == null) {
            return null;
        }

        return socialService.createOAuth2Url(redirectUri, responseType);
    }

    public HttpStatus signUp(AuthContext authContext, String redirectUri) {
        SignUpRequest signUpRequest = null;

        switch (authContext.getProviderType()) {
            case FACEBOOK:
            case VK:
                SocialService socialService = socialServiceFactory.getService(authContext.getProviderType());
                AccessGrant accessGrant = socialService.createAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = socialService.getUserInfo(accessGrant.getUserId(), accessGrant.getAccessToken());

                signUpRequest = new SignUpRequest();

                signUpRequest.setFirstName(userInfo.getFirstName());
                signUpRequest.setLastName(userInfo.getLastName());
                signUpRequest.setMiddleName(userInfo.getMiddleName());

                break;
            case EMAIL_PASSWORD:
                signUpRequest = (SignUpRequest) authContext.getBody();

                break;
        }

        verificationStorage.expire(authContext.getRequest());
        verificationStorage.setAttr(authContext.getRequest(), VerificationStorage.STATE, SessionState.SIGN_UP_CONFIRM);
        verificationStorage.setAttr(authContext.getRequest(), VerificationStorage.SIGN_UP_REQUEST, signUpRequest);

        return HttpStatus.OK;
    }

    public SignUpResult confirmSignUpFinish(AuthContext authContext) throws SQLException {
        SignUpRequest signUpRequest = (SignUpRequest) verificationStorage.getAttr(authContext.getRequest(), VerificationStorage.SIGN_UP_REQUEST);
        SignUpConfirmation signUpConfirmation = (SignUpConfirmation) authContext.getBody();

        if (signUpRequest != null) {
            VerificationResult result = verificationService.verify(
                    authContext.getRequest(),
                    signUpConfirmation.getAuthenticationKey(),
                    signUpConfirmation.getCode()
            );

            if (result.isValid()) {
                User saveUser = new User();

                saveUser.setPhone(signUpConfirmation.getAuthenticationKey().formattedNumber());
                saveUser.setPassword(signUpConfirmation.getPassword());

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

                CookieUtils.addCookie(
                        authContext.getResponse(),
                        uiProperties.getHost(),
                        jwtProperties.tokenName(),
                        token
                );

                authContext.getResponse().addHeader(
                        jwtProperties.tokenName(),
                        token
                );

                return new SignUpResult().setStatus(HttpStatus.OK).setUser(user);
            }

            return new SignUpResult().setStatus(HttpStatus.PRECONDITION_FAILED);
        }

        return new SignUpResult().setStatus(HttpStatus.BAD_REQUEST);
    }

    public SendVerificationResult confirmSignUpStart(HttpServletRequest request, Locale locale, AuthenticationKey authenticationKey) throws MessagingException {
        if (userAccountDetailsService.isExist(authenticationKey)) {
            return new SendVerificationResult(HttpStatus.CONFLICT, null);
        }

        return verificationService.sendVerification(request, locale, authenticationKey);
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
