package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.impl.UserDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.domain.AuthKey;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsServiceImpl service;

    @MockBean
    private UserDao accountDao;

    @MockBean
    private UserRoleDao userRoleDao;

    @MockBean
    private BiographyService biographyService;

    @Test
    void loadUserByUsername() {
        //TODO: Test
    }

    @Test
    void save() throws SQLException {
       /* List<User> db = new ArrayList<>();
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
        assertUserEquals(expected, user);*/
    }

    @Test
    void loadUserById() {
        //TODO: Test
    }

    @Test
    void isExistUserName() {
        Mockito.when(accountDao.isExistEmail(eq("Test"))).thenReturn(true).thenReturn(false);

        AuthKey authKey = new AuthKey();

        authKey.setEmail("Test");
        authKey.setType(AuthKey.Type.EMAIL);

        Assertions.assertTrue(service.isExist(authKey));
        Assertions.assertFalse(service.isExist(authKey));

        Mockito.verify(accountDao, Mockito.times(2)).isExistEmail(eq("Test"));
    }

    @Test
    void loadSocialUserById() {
      /*  User expected = TestModelsUtils.TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Mockito.when(socialAccountDao.getByUserId(eq(1))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)));

        User actual = service.loadSocialUserById(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);*/
    }

    @Test
    void loadSocialUserByAccountId() {
       /* User expected = TestModelsUtils.TEST_USERS.get(TEST_FACEBOOK_USER_ID);

        Mockito.when(socialAccountDao.getByAccountId(eq(ProviderType.FACEBOOK), eq("testId"))).thenReturn(expected);
        Mockito.when(userRoleDao.getRoles(eq(1))).thenReturn(Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)));

        User actual = service.loadSocialUserByAccountId(ProviderType.FACEBOOK, "testId");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);*/
    }

}