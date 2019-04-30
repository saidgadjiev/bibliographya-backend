package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;

import javax.servlet.http.Cookie;
import java.util.*;

public class TestModelsUtils {

    public static final TimeZone TEST_TIME_ZONE = TimeZone.getTimeZone("Europe/Moscow");

    public static final String TEST_AUTH_CODE = "AuthCode";

    public static final String TEST_REDIRECT_URI = "RedirectUri";

    public static final String TEST_SERVER_NAME = "testserver";

    public static final String TEST_JWT_TOKEN = "TestToken";

    public static final String TEST_ACCESS_TOKEN = "TestAccessToken";

    public static final String TEST_FIRST_NAME = "Test";

    public static final String TEST_MIDDLE_NAME = "Test";

    public static final String TEST_LAST_NAME = "Test";

    public static final String TEST_EMAIL = "test@mail.ru";

    public static final AuthKey TEST_AUTHENTICATION_KEY = new AuthKey();

    public static final String TEST_SOCIAL_USER_ID = "socialUserId";

    public static final Integer TEST_USER_ID = 1;

    public static final Map<Integer, User> TEST_USERS = new HashMap<>();

    public static final AccessGrant TEST_ACCESS_GRANT = new AccessGrant(TEST_ACCESS_TOKEN, new Date().getTime(), TEST_SOCIAL_USER_ID);

    public static final Cookie TEST_TOKEN_COOKIE = new Cookie("X-TOKEN", TEST_JWT_TOKEN);

    public static final Cookie TEST_TOKEN_REMOVE_COOKIE = new Cookie("X-TOKEN", null);

    static {

        TEST_AUTHENTICATION_KEY.setEmail(TEST_EMAIL);
        TEST_AUTHENTICATION_KEY.setType(AuthKey.Type.EMAIL);

        TEST_TOKEN_COOKIE.setMaxAge(60 * 60 * 24 * 30);
        TEST_TOKEN_COOKIE.setDomain(TEST_SERVER_NAME);
        TEST_TOKEN_COOKIE.setPath("/");
        TEST_TOKEN_COOKIE.setHttpOnly(true);

        TEST_TOKEN_REMOVE_COOKIE.setMaxAge(0);
        TEST_TOKEN_REMOVE_COOKIE.setDomain(TEST_SERVER_NAME);
        TEST_TOKEN_REMOVE_COOKIE.setPath("/");
        TEST_TOKEN_REMOVE_COOKIE.setHttpOnly(true);

        User user = new User();

        user.setEmail(TEST_AUTHENTICATION_KEY.getEmail());
        user.setId(1);
        user.setPassword("Test");

        user.setBiography(new Biography());
        user.getBiography().setId(1);
        user.getBiography().setFirstName(TEST_FIRST_NAME);
        user.getBiography().setLastName(TEST_LAST_NAME);
        user.getBiography().setMiddleName(TEST_MIDDLE_NAME);
        user.setRoles(Collections.singleton(new Role(Role.ROLE_USER)));

        TEST_USERS.put(TEST_USER_ID, user);
    }

    private TestModelsUtils() {
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
