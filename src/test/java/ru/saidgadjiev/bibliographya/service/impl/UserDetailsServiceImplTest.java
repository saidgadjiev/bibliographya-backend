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
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils.assertUserEquals;
import static ru.saidgadjiev.bibliographya.utils.TestModelsUtils.TEST_EMAIL_USER_ID;
import static ru.saidgadjiev.bibliographya.utils.TestModelsUtils.TEST_FACEBOOK_USER_ID;

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
        User expected = TestModelsUtils.TEST_USERS.get(TEST_EMAIL_USER_ID);

        Mockito.when(accountDao.getByEmail(eq(TestModelsUtils.TEST_EMAIL))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(TEST_EMAIL_USER_ID))).thenReturn(Collections.singleton(new Role(Role.ROLE_USER)));

        User actual = (User) service.loadUserByUsername(TestModelsUtils.TEST_EMAIL);

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

                    user.setId(TEST_EMAIL_USER_ID);
                    user.getUserAccount().setId(1);
                    user.getUserAccount().setUserId(TEST_EMAIL_USER_ID);

                    db.add(user);

                    return user;
                });

        Mockito
                .when(userRoleDao.addRoles(eq(TEST_EMAIL_USER_ID), eq(Collections.singleton(new Role(Role.ROLE_USER)))))
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

                    biography.setId(TEST_EMAIL_USER_ID);
                    biography.setFirstName(request.getFirstName());
                    biography.setLastName(request.getLastName());
                    biography.setMiddleName(request.getMiddleName());
                    biography.setUserId(user.getId());
                    biography.setCreatorId(user.getId());

                    return biography;
                });

        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setEmail(TestModelsUtils.TEST_EMAIL);
        signUpRequest.setPassword("Test");
        signUpRequest.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        signUpRequest.setLastName(TestModelsUtils.TEST_LAST_NAME);
        signUpRequest.setMiddleName(TestModelsUtils.TEST_MIDDLE_NAME);

        User user = service.save(signUpRequest);

        User expected = TestModelsUtils.TEST_USERS.get(TEST_EMAIL_USER_ID);

        Assertions.assertEquals(1, db.size());
        Assertions.assertEquals(1, roles.size());
        assertUserEquals(expected, user);
    }

    @Test
    void loadUserById() {
        User expected = TestModelsUtils.TEST_USERS.get(TEST_EMAIL_USER_ID);

        Mockito.when(accountDao.getByUserId(eq(1))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_USER)));

        User actual = service.loadUserAccountById(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void isExistUserName() {
        Mockito.when(accountDao.isExistEmail(eq("Test"))).thenReturn(true).thenReturn(false);

        Assertions.assertTrue(service.isExistEmail("Test"));
        Assertions.assertFalse(service.isExistEmail("Test"));

        Mockito.verify(accountDao, Mockito.times(2)).isExistEmail(eq("Test"));
    }

    @Test
    void loadSocialUserById() {
        User expected = TestModelsUtils.TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Mockito.when(socialAccountDao.getByUserId(eq(1))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)));

        User actual = service.loadSocialUserById(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loadSocialUserByAccountId() {
        User expected = TestModelsUtils.TEST_USERS.get(TEST_FACEBOOK_USER_ID);

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

        socialUserInfo.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        socialUserInfo.setLastName(TestModelsUtils.TEST_LAST_NAME);
        socialUserInfo.setMiddleName(TestModelsUtils.TEST_MIDDLE_NAME);
        socialUserInfo.setProviderId(ProviderType.FACEBOOK.getId());
        socialUserInfo.setId(TestModelsUtils.TEST_SOCIAL_USER_ID);

        User user = service.saveSocialUser(socialUserInfo);

        User expected = TestModelsUtils.TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Assertions.assertEquals(1, db.size());
        Assertions.assertEquals(1, roles.size());
        assertUserEquals(expected, user);
    }
}