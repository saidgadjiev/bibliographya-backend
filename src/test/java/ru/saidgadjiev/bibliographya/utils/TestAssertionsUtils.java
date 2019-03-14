package ru.saidgadjiev.bibliographya.utils;

import org.junit.jupiter.api.Assertions;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;
import ru.saidgadjiev.bibliographya.domain.User;

import javax.servlet.http.Cookie;

public class TestAssertionsUtils {

    private TestAssertionsUtils() {
    }

    public static void assertCommentsEquals(BiographyComment expected, BiographyComment actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getBiographyId(), actual.getBiographyId());
        Assertions.assertEquals(expected.getContent(), actual.getContent());
        Assertions.assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        Assertions.assertEquals(expected.getParentId(), actual.getParentId());
        Assertions.assertEquals(expected.getUserId(), actual.getUserId());
    }

    public static void assertLikeEquals(BiographyLike expected, BiographyLike actual) {
        Assertions.assertEquals(expected.getUserId(), actual.getUserId());
        Assertions.assertEquals(expected.getBiographyId(), actual.getBiographyId());

        if (expected.getUser() != null) {
            Assertions.assertEquals(expected.getUser().getId(), actual.getUser().getId());
            Assertions.assertEquals(expected.getUser().getFirstName(), actual.getUser().getFirstName());
            Assertions.assertEquals(expected.getUser().getLastName(), actual.getUser().getLastName());
        }
    }

    public static void assertUserEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());

        Assertions.assertEquals(expected.getEmail(), actual.getEmail());
        Assertions.assertEquals(expected.isEmailVerified(), actual.isEmailVerified());

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
