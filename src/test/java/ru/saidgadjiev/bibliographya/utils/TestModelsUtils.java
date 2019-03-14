package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestModelsUtils {

    public static final String TEST_AUTH_CODE = "AuthCode";

    public static final String TEST_REDIRECT_URI = "RedirectUri";

    public static final String TEST_SERVER_NAME = "testserver";

    public static final String TEST_JWT_TOKEN = "TestToken";

    public static final String TEST_ACCESS_TOKEN = "TestAccessToken";

    public static final String TEST_FIRST_NAME = "Test";

    public static final String TEST_MIDDLE_NAME = "Test";

    public static final String TEST_LAST_NAME = "Test";

    public static final String TEST_EMAIL = "test@mail.ru";

    public static final String TEST_SOCIAL_USER_ID = "socialUserId";

    public static final Integer TEST_FACEBOOK_USER_ID = 1;

    public static final Integer TEST_EMAIL_USER_ID = 2;

    public static final Map<Integer, User> TEST_USERS = new HashMap<>();

    public static final AccessGrant TEST_ACCESS_GRANT = new AccessGrant(TEST_ACCESS_TOKEN, new Date().getTime(), TEST_SOCIAL_USER_ID);

    public static final Cookie TEST_TOKEN_COOKIE = new Cookie("X-TOKEN", TEST_JWT_TOKEN);

    public static final Cookie TEST_TOKEN_REMOVE_COOKIE = new Cookie("X-TOKEN", null);

    static {
        TEST_TOKEN_COOKIE.setMaxAge(60 * 60 * 24 * 30);
        TEST_TOKEN_COOKIE.setDomain(TEST_SERVER_NAME);
        TEST_TOKEN_COOKIE.setPath("/");
        TEST_TOKEN_COOKIE.setHttpOnly(true);

        TEST_TOKEN_REMOVE_COOKIE.setMaxAge(0);
        TEST_TOKEN_REMOVE_COOKIE.setDomain(TEST_SERVER_NAME);
        TEST_TOKEN_REMOVE_COOKIE.setPath("/");
        TEST_TOKEN_REMOVE_COOKIE.setHttpOnly(true);
    }

    private TestModelsUtils() {
    }

    public static User createUser(int id,
                                  String firstName,
                                  String lastName,
                                  String middleName,
                                  ProviderType providerType,
                                  Set<Role> roles) {
        User user = new User();

        user.setId(id);
        user.setRoles(roles);

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
                                      Set<Role> roles) {
        return TestModelsUtils.createUser(
                1,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_MIDDLE_NAME,
                providerType,
                roles
        );
    }

    public static SocialUserInfo socialUserInfo() {
        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId(TEST_SOCIAL_USER_ID);
        userInfo.setFirstName(TEST_FIRST_NAME);
        userInfo.setLastName(TEST_LAST_NAME);
        userInfo.setMiddleName(TEST_MIDDLE_NAME);

        return userInfo;
    }
}
