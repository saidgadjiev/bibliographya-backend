package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.model.SignInRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.security.provider.JwtAuthenticationToken;
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
                       SessionManager sessionManager) {
        this.socialServiceFactory = socialServiceFactory;
        this.userAccountDetailsService = userAccountDetailsService;
        this.tokenService = tokenService;
        this.securityService = securityService;
        this.emailVerificationService = emailVerificationService;
        this.uiProperties = uiProperties;
        this.sessionManager = sessionManager;
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

    public User auth(HttpServletResponse response, SignInRequest signInRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getEmail(),
                        signInRequest.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        User user = (User) authentication.getPrincipal();

        auth(response, user);

        return user;
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

        sessionManager.setSignUp(authContext.getRequest(), signUpRequest);

        return HttpStatus.OK;
    }

    public SignUpResult confirmSignUpFinish(AuthContext authContext) throws SQLException {
        SignUpRequest signUpRequest = sessionManager.getSignUp(authContext.getRequest());
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
                sessionManager.removeState(authContext.getRequest());

                auth(authContext.getResponse(), user);

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
        Map<String, Object> claims = tokenService.validate(token);

        try {
            Authentication authenticationToken = authenticationManager.authenticate(
                    new JwtAuthenticationToken(claims)
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (BadCredentialsException ex) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    public void cancelSignUp(HttpServletRequest request) {
        sessionManager.removeState(request);
    }

    private void auth(HttpServletResponse response, User user) {
        securityService.authenticate(user);

        String token = tokenService.createToken(user);

        CookieUtils.addCookie(response, uiProperties.getName(), "X-TOKEN", token);
    }

    public HttpStatus confirmation(HttpServletRequest request) {
        SignUpRequest signUpRequest = sessionManager.getSignUp(request);

        if (signUpRequest == null) {
            return HttpStatus.FOUND;
        }

        return HttpStatus.OK;
    }
}
