package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignInRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.impl.EmailVerificationService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.service.impl.UserDetailsServiceImpl;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.*;
import static ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils.assertCookieEquals;
import static ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils.assertUserEquals;
import static ru.saidgadjiev.bibliographya.utils.TestModelsUtils.*;

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
    private TokenService tokenService;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void getFacebookOauthUrl() {
        Mockito.when(facebookService.createOAuth2Url(eq("test"))).thenReturn("oauth:test:facebook");

        Assertions.assertEquals("oauth:test:facebook", authService.getOauthUrl(ProviderType.FACEBOOK, "test"));
    }

    @Test
    void getVkOauthUrl() {
        Mockito.when(vkService.createOAuth2Url(eq("test"))).thenReturn("oauth:test:vk");

        Assertions.assertEquals("oauth:test:vk", authService.getOauthUrl(ProviderType.VK, "test"));
    }

    private void facebookAuthTest(boolean signUp) throws SQLException {
        SocialUserInfo userInfo = socialUserInfo(ProviderType.FACEBOOK);

        Mockito.when(facebookService.createAccessToken(TEST_AUTH_CODE, TEST_REDIRECT_URI)).thenReturn(TEST_ACCESS_GRANT);
        Mockito.when(facebookService.getUserInfo(null, eq(TEST_ACCESS_TOKEN))).thenReturn(userInfo);

        List<User> db = new ArrayList<>();

        if (signUp) {
            Mockito.when(userDetailsService.saveSocialUser(any())).thenAnswer(invocation -> {
                SocialUserInfo newInfo = (SocialUserInfo) invocation.getArguments()[0];

                SocialAccount socialAccount = new SocialAccount();

                socialAccount.setId(1);
                socialAccount.setAccountId(newInfo.getId());
                socialAccount.setUserId(1);

                db.add(createTestUser(
                        ProviderType.fromId(newInfo.getProviderId()),
                        Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                        null,
                        socialAccount
                ));

                return db.get(0);
            });
        } else {
            User testSocialUser = TEST_USERS.get(TEST_FACEBOOK_USER_ID);

            db.add(testSocialUser);
            Mockito
                    .when(userDetailsService.loadSocialUserByAccountId(ProviderType.FACEBOOK, TEST_SOCIAL_USER_ID))
                    .thenReturn(testSocialUser);

        }

        Mockito.when(tokenService.createToken(any(), any())).thenReturn(TEST_JWT_TOKEN);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getServerName()).thenReturn(TEST_SERVER_NAME);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        List<Cookie> cookies = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            Cookie cookie = (Cookie) invocation.getArguments()[0];

            cookies.add(cookie);

            return null;
        }).when(response).addCookie(any());

        AtomicReference<Authentication> authenticationAtomicReference = new AtomicReference<>();

        Mockito.when(securityService.authenticate(any())).thenAnswer(invocation -> {
            UserDetails userDetails = (UserDetails) invocation.getArguments()[0];
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            authenticationAtomicReference.set(token);

            return token;
        });

        AuthContext authContext = new AuthContext()
                .setRequest(request)
                .setResponse(response)
                .setProviderType(ProviderType.FACEBOOK)
                .setCode(TEST_AUTH_CODE);

        User actual = authService.auth(authContext, TEST_REDIRECT_URI);

        Assertions.assertFalse(db.isEmpty());

        assertCookieEquals(TEST_TOKEN_COOKIE, cookies.get(0));
        assertUserEquals(db.get(0), actual);
        assertUserEquals((User) authenticationAtomicReference.get().getPrincipal(), actual);

        if (signUp) {
            Assertions.assertTrue(actual.getIsNew());
        }

        Assertions.assertNotNull(authenticationAtomicReference.get());
    }

    @Test
    void authWithoutSignUpViaFacebook() throws Exception {
        facebookAuthTest(false);
    }

    @Test
    void authViaFacebook() throws Exception {
        facebookAuthTest(true);
    }

    @Test
    void authViaEmailPassword() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getServerName()).thenReturn(TEST_SERVER_NAME);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        SignInRequest signInRequest = new SignInRequest();

        signInRequest.setEmail(TEST_EMAIL);
        signInRequest.setPassword("Test");

        AuthContext authContext = new AuthContext()
                .setProviderType(ProviderType.EMAIL_PASSWORD)
                .setResponse(response)
                .setRequest(request)
                .setSignInRequest(signInRequest);

        User testUser = TEST_USERS.get(TEST_EMAIL_USER_ID);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                testUser.getAuthorities()
        );

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationToken);
        AtomicReference<Authentication> authenticationAtomicReference = new AtomicReference<>();

        Mockito.when(securityService.authenticate(any())).thenAnswer(invocation -> {
            UserDetails userDetails = (UserDetails) invocation.getArguments()[0];
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            authenticationAtomicReference.set(token);

            return token;
        });

        Mockito.when(tokenService.createToken(any(), any())).thenReturn(TEST_JWT_TOKEN);
        List<Cookie> cookies = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            Cookie cookie = (Cookie) invocation.getArguments()[0];

            cookies.add(cookie);

            return null;
        }).when(response).addCookie(any());

        User actual = authService.auth(authContext, null);

        assertCookieEquals(TEST_TOKEN_COOKIE, cookies.get(0));
        assertUserEquals((User) authenticationAtomicReference.get().getPrincipal(), actual);
        Assertions.assertNotNull(authenticationAtomicReference.get());
    }

    @Test
    void signUp() {
        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setFirstName(TEST_FIRST_NAME);
        signUpRequest.setLastName(TEST_LAST_NAME);
        signUpRequest.setMiddleName(TEST_MIDDLE_NAME);
        signUpRequest.setEmail(TEST_EMAIL);
        signUpRequest.setPassword("test");

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        Mockito.when(request.getSession(eq(true))).thenReturn(session);

        Map<String, Object> attrs = new HashMap<>();

        Mockito.doAnswer(invocation -> {
            String key = (String) invocation.getArguments()[0];
            Object value = invocation.getArguments()[1];

            attrs.put(key, value);

            return null;
        }).when(session).setAttribute(anyString(), any());

        authService.signUp(request, Locale.getDefault(), signUpRequest);

        Assertions.assertEquals(2, attrs.size());

        Mockito.verify(emailVerificationService, Mockito.times(1)).sendVerification(eq(TEST_EMAIL));

        Assertions.assertTrue((Boolean) attrs.get("signingUp"));

        SignUpRequest actual = (SignUpRequest) attrs.get("request");

        Assertions.assertEquals(signUpRequest.getEmail(), actual.getEmail());
        Assertions.assertEquals(signUpRequest.getFirstName(), actual.getFirstName());
        Assertions.assertEquals(signUpRequest.getLastName(), actual.getLastName());
        Assertions.assertEquals(signUpRequest.getMiddleName(), actual.getMiddleName());
        Assertions.assertEquals(signUpRequest.getPassword(), actual.getPassword());
    }

    @Test
    void confirmSignUpWithValidCode() throws SQLException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        Mockito.when(request.getSession(eq(false))).thenReturn(session);
        Mockito.when(session.getAttribute(eq("state"))).thenReturn(SessionState.SIGN_UP_CONFIRM);

        SignUpRequest signUpRequest = signUpRequest();

        Mockito.when(session.getAttribute(eq("request"))).thenReturn(signUpRequest);
        Mockito.when(emailVerificationService.confirm(eq(TEST_EMAIL), eq(1024)))
                .thenReturn(new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID));

        List<User> db = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            SignUpRequest req = (SignUpRequest) invocation.getArguments()[0];

            UserAccount account = new UserAccount();

            account.setPassword(req.getPassword());
            account.setEmail(req.getEmail());
            account.setUserId(1);
            account.setId(1);

            db.add(createUser(
                    1,
                    req.getFirstName(),
                    req.getLastName(),
                    req.getMiddleName(),
                    ProviderType.EMAIL_PASSWORD,
                    Collections.singleton(new Role(Role.ROLE_USER)),
                    account,
                    null
            ));

            return null;
        }).when(userDetailsService).save(any(SignUpRequest.class));

        SignUpResult signUpResult = authService.confirmSignUp(request, 1024);

        Assertions.assertEquals(HttpStatus.OK, signUpResult.getStatus());
        Assertions.assertFalse(db.isEmpty());
        Mockito.verify(session, Mockito.times(1)).invalidate();
    }

    @Test
    void confirmSignUpWithInValidCode() throws SQLException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        Mockito.when(request.getSession(eq(false))).thenReturn(session);
        Mockito.when(session.getAttribute(eq("state"))).thenReturn(SessionState.SIGN_UP_CONFIRM);

        SignUpRequest signUpRequest = signUpRequest();

        Mockito.when(session.getAttribute(eq("request"))).thenReturn(signUpRequest);
        Mockito.when(emailVerificationService.confirm(eq(TEST_EMAIL), eq(1024)))
                .thenReturn(new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID));

        SignUpResult signUpResult = authService.confirmSignUp(request, 1024);

        Assertions.assertEquals(HttpStatus.PRECONDITION_FAILED, signUpResult.getStatus());
        Mockito.verify(session, Mockito.never()).invalidate();
        Mockito.verify(userDetailsService, Mockito.never()).save(any(SignUpRequest.class));
    }

    @Test
    void confirmSignUpWithoutSession() throws SQLException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(emailVerificationService.confirm(eq(TEST_EMAIL), eq(1024)))
                .thenReturn(new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID));

        SignUpResult signUpResult = authService.confirmSignUp(request, 1024);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, signUpResult.getStatus());
        Mockito.verify(userDetailsService, Mockito.never()).save(any(SignUpRequest.class));
    }

    @Test
    void signOut() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getServerName()).thenReturn(TEST_SERVER_NAME);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        List<Cookie> cookies = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            Cookie cookie = (Cookie) invocation.getArguments()[0];

            cookies.add(cookie);

            return null;
        }).when(response).addCookie(any(Cookie.class));

        User testUser = TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Authentication authentication = authenticate(testUser);

        Mockito.when(securityService.findLoggedInUserAuthentication()).thenReturn(authentication);

        User actual = authService.signOut(request, response);

        Assertions.assertFalse(cookies.isEmpty());

        assertCookieEquals(TEST_TOKEN_REMOVE_COOKIE, cookies.get(0));
        assertUserEquals(testUser, actual);
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void anonymousSignOut() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        User actual = authService.signOut(request, response);
        Assertions.assertNull(actual);
    }

    @Test
    void signedInAccount() {
        User testUser = TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Mockito.when(securityService.findLoggedInUser()).thenReturn(testUser);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.OK);
        assertUserEquals(testUser, (User) accountResult.getBody());
    }

    @Test
    void anonymousAccount() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.NOT_FOUND);
        Assertions.assertNull(accountResult.getBody());
    }

    @Test
    void signingUpAccount() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        Mockito.when(request.getSession(anyBoolean())).thenReturn(session);
        Mockito.when(session.getAttribute(eq("state"))).thenReturn(SessionState.SIGN_UP_CONFIRM);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.PRECONDITION_REQUIRED);
        Assertions.assertNull(accountResult.getBody());
    }

    @Test
    void tokenAuth() {
    }

    private Authentication authenticate(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    private SignUpRequest signUpRequest() {
        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setFirstName(TEST_FIRST_NAME);
        signUpRequest.setLastName(TEST_LAST_NAME);
        signUpRequest.setMiddleName(TEST_MIDDLE_NAME);
        signUpRequest.setEmail(TEST_EMAIL);
        signUpRequest.setPassword("test");

        return signUpRequest;
    }
}