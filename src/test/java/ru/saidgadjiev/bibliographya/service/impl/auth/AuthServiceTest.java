package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaTestConfiguration;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.service.impl.UserDetailsServiceImpl;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.*;
import static ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils.assertCookieEquals;
import static ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils.assertUserEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(BibliographyaTestConfiguration.class)
class AuthServiceTest {

    private static final String TEST_FIRST_NAME = "Test";

    private static final String TEST_MIDDLE_NAME = "Test";

    private static final String TEST_LAST_NAME = "Test";

    private static final String TEST_JWT_TOKEN = "TestToken";

    private static final String TEST_ACCESS_TOKEN = "TestAccessToken";

    private static final String TEST_SOCIAL_USER_ID = "socialUserId";

    private static final String TEST_AUTH_CODE = "AuthCode";

    private static final String TEST_REDIRECT_URI = "RedirectUri";

    private static final String TEST_SERVER_NAME = "testserver";

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

    @Test
    void getFacebookOauthUrl() {
        Mockito.when(facebookService.createFacebookAuthorizationUrl(eq("test"))).thenReturn("oauth:test:facebook");

        Assertions.assertEquals("oauth:test:facebook", authService.getOauthUrl(ProviderType.FACEBOOK, "test"));
    }

    @Test
    void getVkOauthUrl() {
        Mockito.when(vkService.createVKAuthorizationUrl(eq("test"))).thenReturn("oauth:test:vk");

        Assertions.assertEquals("oauth:test:vk", authService.getOauthUrl(ProviderType.VK, "test"));
    }

    @Test
    void authWithSignUpViaFacebook() throws Exception {
    }

    @Test
    void authViaFacebook() throws Exception {
        AccessGrant accessGrant = new AccessGrant(TEST_ACCESS_TOKEN, new Date().getTime(), TEST_SOCIAL_USER_ID);
        SocialUserInfo userInfo = socialUserInfo(ProviderType.FACEBOOK);

        Mockito.when(facebookService.createFacebookAccessToken(TEST_AUTH_CODE, TEST_REDIRECT_URI)).thenReturn(accessGrant);
        Mockito.when(facebookService.getUserInfo(eq(TEST_ACCESS_TOKEN))).thenReturn(userInfo);

        List<User> db = new ArrayList<>();

        Mockito.when(userDetailsService.saveSocialUser(any())).thenAnswer(invocation -> {
            SocialUserInfo newInfo = (SocialUserInfo) invocation.getArguments()[0];

            SocialAccount socialAccount = new SocialAccount();

            socialAccount.setId(1);
            socialAccount.setAccountId(newInfo.getId());
            socialAccount.setUserId(1);

            db.add(createUser(
                    ProviderType.fromId(newInfo.getProviderId()),
                    Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                    null,
                    socialAccount
            ));

            return db.get(0);
        });

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

        Mockito.when(securityService.authenticate(any())).thenAnswer(new Answer<Authentication>() {
            @Override
            public Authentication answer(InvocationOnMock invocation) throws Throwable {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        invocation.getArguments()[0], null, (Collection<? extends GrantedAuthority>) invocation.getArguments()[1]
                );

                authenticationAtomicReference.set(token);

                return token;
            }
        });

        AuthContext authContext = new AuthContext()
                .setRequest(request)
                .setResponse(response)
                .setProviderType(ProviderType.FACEBOOK)
                .setCode(TEST_AUTH_CODE);

        User actual = authService.auth(authContext, TEST_REDIRECT_URI);

        Assertions.assertFalse(db.isEmpty());

        assertCookieEquals(createTokenCookie(false), cookies.get(0));
        assertUserEquals(db.get(0), actual);
        assertUserEquals((User) authenticationAtomicReference.get().getPrincipal(), actual);

        Assertions.assertNotNull(authenticationAtomicReference.get());
    }

    @Test
    void signUp() {
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

        SocialAccount socialAccount = socialAccount();

        User testUser = createUser(
                ProviderType.FACEBOOK,
                Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                null,
                socialAccount
        );

        Authentication authentication = authenticate(testUser);

        Mockito.when(securityService.findLoggedInUserAuthentication()).thenReturn(authentication);

        User actual = authService.signOut(request, response);

        Assertions.assertFalse(cookies.isEmpty());

        assertCookieEquals(createTokenCookie(true), cookies.get(0));
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
        SocialAccount socialAccount = socialAccount();

        User testUser = createUser(
                ProviderType.FACEBOOK,
                Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                null,
                socialAccount
        );

        Mockito.when(securityService.findLoggedInUser()).thenReturn(testUser);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.OK);
        assertUserEquals(testUser, accountResult.getAccount());
    }

    @Test
    void anonymousAccount() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.NOT_FOUND);
        Assertions.assertNull(accountResult.getAccount());
    }

    @Test
    void signingUpAccount() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        Mockito.when(request.getSession(anyBoolean())).thenReturn(session);
        Mockito.when(session.getAttribute(eq(AuthService.SESSION_SIGNING_UP))).thenReturn(true);

        AccountResult accountResult = authService.account(request);

        Assertions.assertEquals(accountResult.getStatus(), HttpStatus.PRECONDITION_REQUIRED);
        Assertions.assertNull(accountResult.getAccount());
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

    private User createUser(ProviderType providerType,
                            Set<Role> roles,
                            UserAccount userAccount,
                            SocialAccount socialAccount) {
        return TestModelsUtils.createUser(1, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_MIDDLE_NAME, providerType, roles, userAccount, socialAccount);
    }

    private Cookie createTokenCookie(boolean delete) {
        Cookie cookie = new Cookie("X-TOKEN", delete ? null : TEST_JWT_TOKEN);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain(TEST_SERVER_NAME);
        cookie.setMaxAge(delete ? 0 : 60 * 60 * 24 * 30);

        return cookie;
    }

    private SocialUserInfo socialUserInfo(ProviderType providerType) {
        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId(TEST_SOCIAL_USER_ID);
        userInfo.setProviderId(providerType.getId());
        userInfo.setFirstName(TEST_FIRST_NAME);
        userInfo.setLastName(TEST_LAST_NAME);
        userInfo.setMiddleName(TEST_MIDDLE_NAME);

        return userInfo;
    }

    private SocialAccount socialAccount() {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setUserId(1);
        socialAccount.setAccountId(TEST_SOCIAL_USER_ID);
        socialAccount.setId(1);

        return socialAccount;
    }
}