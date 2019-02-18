package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.dao.impl.SocialAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsServiceImpl service;

    @MockBean
    private UserAccountDao accountDao;

    @MockBean
    private SocialAccountDao socialAccountDao;

    @MockBean
    private UserRoleDao userRoleDao;

    @MockBean
    private BiographyService biographyService;

    @Test
    void loadUserByUsername() {
        UserAccount userAccount = new UserAccount();

        userAccount.setName("Test");
        userAccount.setUserId(1);
        userAccount.setId(1);
        userAccount.setPassword("Test");

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.USERNAME_PASSWORD,
                Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()),
                userAccount,
                null
        );

        Mockito.when(accountDao.getByUsername(eq("Test"))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_USER)));

        User actual = (User) service.loadUserByUsername("Test");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void save() throws SQLException {
        List<User> db = new ArrayList<>();
        List<Role> roles = new ArrayList<>();

        Mockito
                .when(accountDao.save(isA(User.class)))
                .thenAnswer(invocationOnMock -> {
                    User user = (User) invocationOnMock.getArguments()[0];

                    user.setId(1);
                    user.getUserAccount().setId(1);
                    user.getUserAccount().setUserId(1);

                    db.add(user);

                    return user;
                });

        Mockito
                .when(userRoleDao.addRoles(eq(1), eq(Collections.singleton(new Role(Role.ROLE_USER)))))
                .thenAnswer(invocationOnMock -> {
                    Collection<Role> createRoles = (Collection<Role>) invocationOnMock.getArguments()[1];

                    roles.addAll(createRoles);

                    return null;
                });

        Mockito
                .when(biographyService.createAccountBiography(isA(User.class), isA(BiographyRequest.class)))
                .thenAnswer(invocationOnMock -> {
                    BiographyRequest request = (BiographyRequest) invocationOnMock.getArguments()[1];
                    User user = (User) invocationOnMock.getArguments()[0];
                    Biography biography = new Biography();

                    biography.setId(1);
                    biography.setFirstName(request.getFirstName());
                    biography.setLastName(request.getLastName());
                    biography.setMiddleName(request.getMiddleName());
                    biography.setUserId(user.getId());
                    biography.setCreatorId(user.getId());

                    return biography;
                });

        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setUsername("Test");
        signUpRequest.setPassword("Test");
        signUpRequest.setFirstName("Test");
        signUpRequest.setLastName("Test");
        signUpRequest.setMiddleName("Test");

        User user = service.save(signUpRequest);

        UserAccount userAccount = new UserAccount();

        userAccount.setName("Test");
        userAccount.setUserId(1);
        userAccount.setId(1);
        userAccount.setPassword("Test");

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.USERNAME_PASSWORD,
                Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()),
                userAccount,
                null
        );

        Assertions.assertEquals(1, db.size());
        Assertions.assertEquals(1, roles.size());
        assertEquals(expected, user);
    }

    @Test
    void loadUserById() {
        UserAccount userAccount = new UserAccount();

        userAccount.setName("Test");
        userAccount.setUserId(1);
        userAccount.setId(1);
        userAccount.setPassword("Test");

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.USERNAME_PASSWORD,
                Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()),
                userAccount,
                null
        );

        Mockito.when(accountDao.getByUserId(eq(1))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_USER)));

        User actual = service.loadUserById(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void isExistUserName() {
        Mockito.when(accountDao.isExistUsername(eq("Test"))).thenReturn(true).thenReturn(false);

        Assertions.assertTrue(service.isExistUserName("Test"));
        Assertions.assertFalse(service.isExistUserName("Test"));

        Mockito.verify(accountDao, Mockito.times(2)).isExistUsername(eq("Test"));
    }

    @Test
    void loadSocialUserById() {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId("testId");
        socialAccount.setUserId(1);
        socialAccount.setId(1);

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.FACEBOOK,
                Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()),
                null,
                socialAccount
        );

        Mockito.when(socialAccountDao.getByUserId(eq(1))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)));

        User actual = service.loadSocialUserById(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loadSocialUserByAccountId() {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId("testId");
        socialAccount.setUserId(1);
        socialAccount.setId(1);

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.FACEBOOK,
                Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()),
                null,
                socialAccount
        );

        Mockito.when(socialAccountDao.getByAccountId(eq(ProviderType.FACEBOOK), eq("testId"))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)));

        User actual = service.loadSocialUserByAccountId(ProviderType.FACEBOOK, "testId");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void saveSocialUser() throws SQLException {
        List<User> db = new ArrayList<>();
        List<Role> roles = new ArrayList<>();

        Mockito
                .when(socialAccountDao.save(isA(User.class)))
                .thenAnswer(invocationOnMock -> {
                    User user = (User) invocationOnMock.getArguments()[0];

                    user.setId(1);
                    user.getSocialAccount().setId(1);
                    user.getSocialAccount().setUserId(1);

                    db.add(user);

                    return user;
                });

        Mockito
                .when(userRoleDao.addRoles(eq(1), eq(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)))))
                .thenAnswer(invocationOnMock -> {
                    Collection<Role> createRoles = (Collection<Role>) invocationOnMock.getArguments()[1];

                    roles.addAll(createRoles);

                    return null;
                });

        Mockito
                .when(biographyService.createAccountBiography(isA(User.class), isA(BiographyRequest.class)))
                .thenAnswer(invocationOnMock -> {
                    BiographyRequest request = (BiographyRequest) invocationOnMock.getArguments()[1];
                    User user = (User) invocationOnMock.getArguments()[0];
                    Biography biography = new Biography();

                    biography.setId(1);
                    biography.setFirstName(request.getFirstName());
                    biography.setLastName(request.getLastName());
                    biography.setMiddleName(request.getMiddleName());
                    biography.setUserId(user.getId());
                    biography.setCreatorId(user.getId());

                    return biography;
                });

        SocialUserInfo socialUserInfo = new SocialUserInfo();

        socialUserInfo.setFirstName("Test");
        socialUserInfo.setLastName("Test");
        socialUserInfo.setMiddleName("Test");
        socialUserInfo.setProviderId(ProviderType.FACEBOOK.getId());
        socialUserInfo.setId("testId");

        User user = service.saveSocialUser(socialUserInfo);

        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId("testId");
        socialAccount.setUserId(1);
        socialAccount.setId(1);

        User expected = createUser(
                1,
                "Test",
                "Test",
                "Test",
                ProviderType.FACEBOOK,
                Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()),
                null,
                socialAccount
        );

        Assertions.assertEquals(1, db.size());
        Assertions.assertEquals(1, roles.size());
        assertEquals(expected, user);
    }

    private void assertEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType(), actual.getProviderType());

        if (expected.getUserAccount() != null) {
            Assertions.assertNotNull(actual.getUserAccount());

            Assertions.assertEquals(expected.getUserAccount().getId(), actual.getUserAccount().getId());
            Assertions.assertEquals(expected.getUserAccount().getName(), actual.getUserAccount().getName());
        }

        if (expected.getSocialAccount() != null) {
            Assertions.assertNotNull(actual.getSocialAccount());

            Assertions.assertEquals(expected.getSocialAccount().getId(), actual.getSocialAccount().getId());
            Assertions.assertEquals(expected.getSocialAccount().getAccountId(), actual.getSocialAccount().getAccountId());
            Assertions.assertEquals(expected.getSocialAccount().getUserId(), actual.getSocialAccount().getUserId());
        }

        Assertions.assertEquals(expected.getRoles(), actual.getRoles());
        Assertions.assertEquals(expected.getBiography().getId(), actual.getBiography().getId());
        Assertions.assertEquals(expected.getBiography().getFirstName(), actual.getBiography().getFirstName());
        Assertions.assertEquals(expected.getBiography().getLastName(), actual.getBiography().getLastName());
        Assertions.assertEquals(expected.getBiography().getMiddleName(), actual.getBiography().getMiddleName());
        Assertions.assertEquals(expected.getBiography().getUserId(), actual.getBiography().getUserId());
        Assertions.assertEquals(expected.getBiography().getCreatorId(), actual.getBiography().getCreatorId());
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