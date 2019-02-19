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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(BibliographyaTestConfiguration.class)
class AuthServiceTest {

    private static final String TEST_JWT_TOKEN = "TestToken";

    private static final String TEST_ACCESS_TOKEN = "TestAccessToken";

    private static final String TEST_FACEBOOK_USER_ID = "facebookUserId";

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
        AccessGrant accessGrant = new AccessGrant(TEST_ACCESS_TOKEN, new Date().getTime(), TEST_FACEBOOK_USER_ID);
        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId(TEST_FACEBOOK_USER_ID);
        userInfo.setProviderId(ProviderType.FACEBOOK.getId());
        userInfo.setFirstName("Test");
        userInfo.setLastName("Test");

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
                    1,
                    newInfo.getFirstName(),
                    newInfo.getLastName(),
                    newInfo.getMiddleName(),
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

        Mockito.when(securityService.authenticate(any(), anySet())).thenAnswer(new Answer<Authentication>() {
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
        assertCoookieEquals(cookies.get(0));
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
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        authService.signOut(request, response);
    }

    @Test
    void signedInAccount() {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setId(1);
        socialAccount.setAccountId(TEST_FACEBOOK_USER_ID);
        socialAccount.setUserId(1);

        User testUser = createUser(
                1,
                "Test",
                "Test",
                "Test",
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

    private void assertCoookieEquals(Cookie cookie) {
        Assertions.assertEquals(cookie.getName(), "X-TOKEN");
        Assertions.assertEquals(cookie.getValue(), TEST_JWT_TOKEN);
        Assertions.assertTrue(cookie.isHttpOnly());
        Assertions.assertEquals(cookie.getDomain(), TEST_SERVER_NAME);
        Assertions.assertEquals(cookie.getPath(), "/");
    }

    private void assertUserEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType().getId(), actual.getProviderType().getId());
        Assertions.assertIterableEquals(expected.getRoles(), actual.getRoles());

        Assertions.assertEquals(expected.getBiography().getId(), actual.getBiography().getId());
        Assertions.assertEquals(expected.getBiography().getFirstName(), actual.getBiography().getFirstName());
        Assertions.assertEquals(expected.getBiography().getLastName(), actual.getBiography().getLastName());
        Assertions.assertEquals(expected.getBiography().getMiddleName(), actual.getBiography().getMiddleName());
        Assertions.assertEquals(expected.getBiography().getUserId(), actual.getBiography().getUserId());
        Assertions.assertEquals(expected.getBiography().getCreatorId(), actual.getBiography().getCreatorId());

        Assertions.assertEquals(expected.getSocialAccount().getAccountId(), actual.getSocialAccount().getAccountId());
        Assertions.assertEquals(expected.getSocialAccount().getId(), actual.getSocialAccount().getId());
        Assertions.assertEquals(expected.getSocialAccount().getUserId(), actual.getSocialAccount().getUserId());
    }

    private User createUser(int id,
                            String firstName,
                            String lastName,
                            String middleName,
                            ProviderType providerType,
                            Set<Role> roles,
                            UserAccount userAccount,
                            SocialAccount socialAccount) {
        User user = new User();

        user.setId(1);
        user.setRoles(roles);
        user.setUserAccount(userAccount);
        user.setSocialAccount(socialAccount);
        user.setProviderType(providerType);

        Biography biography = new Biography();

        biography.setId(id);
        biography.setFirstName(firstName);
        biography.setLastName(lastName);
        biography.setMiddleName(middleName);
        biography.setUserId(id);
        biography.setCreatorId(id);

        user.setBiography(biography);

        return user;
    }
}