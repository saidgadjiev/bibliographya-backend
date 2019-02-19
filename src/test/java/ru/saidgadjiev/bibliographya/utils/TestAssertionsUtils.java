package ru.saidgadjiev.bibliographya.utils;

import org.junit.jupiter.api.Assertions;
import ru.saidgadjiev.bibliographya.domain.User;

import javax.servlet.http.Cookie;

public class TestAssertionsUtils {

    private TestAssertionsUtils() {
    }

    public static void assertUserEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType(), actual.getProviderType());

        if (expected.getUserAccount() != null) {
            Assertions.assertNotNull(actual.getUserAccount());

            Assertions.assertEquals(expected.getUserAccount().getId(), actual.getUserAccount().getId());
            Assertions.assertEquals(expected.getUserAccount().getEmail(), actual.getUserAccount().getEmail());
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

    public static void assertCookieEquals(Cookie expected, Cookie actual) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getValue(), actual.getValue());
        Assertions.assertEquals(expected.isHttpOnly(), actual.isHttpOnly());
        Assertions.assertEquals(expected.getDomain(), actual.getDomain());
        Assertions.assertEquals(expected.getPath(), actual.getPath());
        Assertions.assertEquals(expected.getMaxAge(), actual.getMaxAge());
    }
}
