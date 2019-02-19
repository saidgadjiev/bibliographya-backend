package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestModelsUtils {

    public static final String TEST_FIRST_NAME = "Test";

    public static final String TEST_MIDDLE_NAME = "Test";

    public static final String TEST_LAST_NAME = "Test";

    public static final String TEST_EMAIL = "test@mail.ru";

    public static final String TEST_SOCIAL_USER_ID = "socialUserId";

    public static final Integer TEST_FACEBOOK_USER_ID = 1;

    public static final Integer TEST_EMAIL_USER_ID = 1;

    public static final Map<Integer, User> TEST_USERS = new HashMap<>();

    static {
        SocialAccount facebookSocialAccount = new SocialAccount();

        facebookSocialAccount.setUserId(TEST_FACEBOOK_USER_ID);
        facebookSocialAccount.setAccountId(TEST_SOCIAL_USER_ID);
        facebookSocialAccount.setId(1);

        TEST_USERS.put(
                TEST_FACEBOOK_USER_ID,
                createUser(
                        TEST_FACEBOOK_USER_ID,
                        TEST_FIRST_NAME,
                        TEST_LAST_NAME,
                        TEST_MIDDLE_NAME,
                        ProviderType.FACEBOOK,
                        Collections.singleton(new Role(Role.ROLE_SOCIAL_USER)),
                        null,
                        facebookSocialAccount

                ));

        UserAccount userAccount = new UserAccount();

        userAccount.setEmail(TEST_EMAIL);
        userAccount.setUserId(TEST_EMAIL_USER_ID);
        userAccount.setId(1);
        userAccount.setPassword("Test");

        TEST_USERS.put(
                TEST_EMAIL_USER_ID,
                createUser(
                        TEST_EMAIL_USER_ID,
                        TEST_FIRST_NAME,
                        TEST_LAST_NAME,
                        TEST_MIDDLE_NAME,
                        ProviderType.EMAIL_PASSWORD,
                        Collections.singleton(new Role(Role.ROLE_USER)),
                        userAccount,
                        null

                ));
    }

    private TestModelsUtils() {
    }

    public static User createUser(int id,
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

    public static User createTestUser(ProviderType providerType,
                                      Set<Role> roles,
                                      UserAccount userAccount,
                                      SocialAccount socialAccount) {
        return TestModelsUtils.createUser(
                1,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_MIDDLE_NAME,
                providerType,
                roles,
                userAccount,
                socialAccount
        );
    }

    public static SocialAccount socialAccount(int id, int userId) {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setUserId(userId);
        socialAccount.setAccountId(TEST_SOCIAL_USER_ID);
        socialAccount.setId(id);

        return socialAccount;
    }
}
