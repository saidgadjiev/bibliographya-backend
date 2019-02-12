package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

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
    private BibliographyaUserDetailsService userDetailsService;

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
    void authViaFacebook() throws Exception {
        AccessGrant accessGrant = new AccessGrant("testToken", new Date().getTime(), "testUserId");
        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId("testUserId");
        userInfo.setProviderId(ProviderType.FACEBOOK.getId());
        userInfo.setFirstName("Test");
        userInfo.setLastName("Test");

        Mockito.when(facebookService.createFacebookAccessToken("testCode", "testUrl")).thenReturn(accessGrant);
        Mockito.when(facebookService.getUserInfo(eq("testToken"))).thenReturn(userInfo);
        Mockito.when(userDetailsService.loadSocialUserByAccountId(eq(ProviderType.FACEBOOK), eq(userInfo.getId()))).thenReturn(null);
        Mockito.when(userDetailsService.saveSocialUser(any())).thenAnswer(invocation -> {
            SocialUserInfo newInfo = (SocialUserInfo) invocation.getArguments()[0];

            SocialAccount socialAccount = new SocialAccount();

            socialAccount.setId(1);
            socialAccount.setAccountId(newInfo.getId());
            socialAccount.setUserId(1);

            return createUser(
                    1,
                    newInfo.getFirstName(),
                    newInfo.getLastName(),
                    newInfo.getMiddleName(),
                    ProviderType.fromId(newInfo.getProviderId()),
                    Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                    null,
                    socialAccount
            );
        });

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        AuthContext authContext = new AuthContext()
                .setRequest(request)
                .setResponse(response)
                .setProviderType(ProviderType.FACEBOOK)
                .setCode("testCode");

        authService.auth(authContext, "testUrl");
    }

    @Test
    void signUp() {
    }

    @Test
    void signOut() {
    }

    @Test
    void account() {
    }

    @Test
    void tokenAuth() {
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