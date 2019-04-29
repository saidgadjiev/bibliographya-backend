package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.ResponseType;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.UserDetailsServiceImpl;
import ru.saidgadjiev.bibliographya.service.impl.VerificationServiceWrapper;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;
import ru.saidgadjiev.bibliographya.service.impl.verification.SessionVerificationStorage;
import ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private FacebookService facebookService;

    @MockBean
    private VKService vkService;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthTokenService tokenService;

    @MockBean
    private VerificationServiceWrapper emailVerificationService;

    @MockBean
    private SessionVerificationStorage sessionManager;

    @MockBean
    private UIProperties uiProperties;

    @MockBean
    private JwtProperties jwtProperties;


    @Test
    void getFacebookOauthUrl() {
        Mockito.when(facebookService.createOAuth2Url(eq("test"), eq(ResponseType.AUTHORIZATION_CODE))).thenReturn("oauth:test:facebook");

        Assertions.assertEquals("oauth:test:facebook", authService.getOauthUrl(ProviderType.FACEBOOK, "test", ResponseType.AUTHORIZATION_CODE));
    }

    @Test
    void getVkOauthUrl() {
        Mockito.when(vkService.createOAuth2Url(eq("test"), eq(ResponseType.AUTHORIZATION_CODE))).thenReturn("oauth:test:vk");

        Assertions.assertEquals("oauth:test:vk", authService.getOauthUrl(ProviderType.VK, "test", ResponseType.AUTHORIZATION_CODE));
    }


    @Test
    void signUp() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        signUpRequest.setLastName(TestModelsUtils.TEST_LAST_NAME);
        signUpRequest.setMiddleName(TestModelsUtils.TEST_MIDDLE_NAME);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        AuthContext authContext = new AuthContext()
                .setProviderType(ProviderType.EMAIL_PASSWORD)
                .setRequest(request)
                .setBody(signUpRequest);

        authService.signUp(authContext, null);

        Mockito.verify(sessionManager, Mockito.times(1)).setAttr(request, VerificationStorage.SIGN_UP_REQUEST, signUpRequest);
    }

    @Test
    void confirmSignUpStart() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Locale locale = new Locale("ru", "RU");

        Mockito.when(userDetailsService.isExist(TestModelsUtils.TEST_AUTHENTICATION_KEY)).thenReturn(false);

        SendVerificationResult status = authService.confirmSignUpStart(request, locale, TestModelsUtils.TEST_AUTHENTICATION_KEY);

        Assertions.assertEquals(status.getStatus(), HttpStatus.OK);
        Mockito.verify(emailVerificationService, Mockito.times(1)).sendVerification(request, locale, TestModelsUtils.TEST_AUTHENTICATION_KEY);
    }

    @Test
    void confirmSignUpStartEmailConflict() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Locale locale = new Locale("ru", "RU");

        Mockito.when(userDetailsService.isExist(TestModelsUtils.TEST_AUTHENTICATION_KEY)).thenReturn(true);

        SendVerificationResult status = authService.confirmSignUpStart(request, Locale.ENGLISH, TestModelsUtils.TEST_AUTHENTICATION_KEY);

        Assertions.assertEquals(status.getStatus(), HttpStatus.CONFLICT);
        Mockito.verify(emailVerificationService, Mockito.times(0)).sendVerification(request, locale, TestModelsUtils.TEST_AUTHENTICATION_KEY);
    }

    @Test
    void confirmSignUpFinish() throws SQLException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        signUpRequest.setLastName(TestModelsUtils.TEST_LAST_NAME);
        signUpRequest.setMiddleName(TestModelsUtils.TEST_MIDDLE_NAME);

        Mockito.when(sessionManager.getAttr(request, VerificationStorage.SIGN_UP_REQUEST)).thenReturn(signUpRequest);
        Mockito.when(emailVerificationService.verify(any(), eq(1234), false))
                .thenReturn(new VerificationResult().setStatus(VerificationResult.Status.VALID));

        List<User> db = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            User req = (User) invocation.getArguments()[0];

            User user = new User();

            user.setPassword(req.getPassword());
            user.setEmail(req.getEmail());
            user.setId(1);

            user.setBiography(req.getBiography());
            user.getBiography().setId(1);
            user.setRoles(Collections.singleton(new Role(Role.ROLE_USER)));

            db.add(user);

            return user;
        }).when(userDetailsService).save(any(User.class));

        Mockito.when(uiProperties.getHost()).thenReturn(TestModelsUtils.TEST_SERVER_NAME);
        Mockito.when(jwtProperties.tokenName()).thenReturn(TestModelsUtils.TEST_TOKEN_COOKIE.getName());
        Mockito.when(tokenService.createToken(any())).thenReturn(TestModelsUtils.TEST_JWT_TOKEN);

        List<Cookie> cookies = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            Cookie cookie = (Cookie) invocation.getArguments()[0];

            cookies.add(cookie);

            return null;
        }).when(response).addCookie(any());

        AuthContext authContext = new AuthContext();

        authContext.setRequest(request);
        authContext.setResponse(response);

        SignUpConfirmation signUpConfirmation = new SignUpConfirmation();

        signUpConfirmation.setPassword("test");
        signUpConfirmation.setCode(1234);

        authContext.setBody(signUpConfirmation);

        SignUpResult signUpResult = authService.confirmSignUpFinish(authContext);

        Assertions.assertEquals(signUpResult.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(1, db.size());

        User expected = new User();

        expected.setId(TestModelsUtils.TEST_USER_ID);
        expected.setEmail(TestModelsUtils.TEST_EMAIL);
        expected.setPassword("Test");

        expected.setRoles(Collections.singleton(new Role(Role.ROLE_USER)));
        expected.setBiography(new Biography());
        expected.getBiography().setId(1);
        expected.getBiography().setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        expected.getBiography().setLastName(TestModelsUtils.TEST_LAST_NAME);
        expected.getBiography().setMiddleName(TestModelsUtils.TEST_MIDDLE_NAME);

        TestAssertionsUtils.assertUserEquals(expected, signUpResult.getUser());
        TestAssertionsUtils.assertUserEquals(expected, db.get(0));
        TestAssertionsUtils.assertCookieEquals(TestModelsUtils.TEST_TOKEN_COOKIE, cookies.get(0));
        Mockito.verify(securityService, Mockito.times(1)).autoLogin(db.get(0));
    }
}