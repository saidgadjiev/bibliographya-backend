package ru.saidgadjiev.bibliographya.utils;

import org.apache.commons.lang.StringUtils;

public class SecureUtils {

    private SecureUtils() {}

    public static String secureEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        String domain = email.substring(email.indexOf('@'));

        email = email.substring(0, 2) + "***" + domain;

        return email;
    }

    public static String securePhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return "+" + phone.substring(0, 1)
                + StringUtils.repeat("*", phone.length() - 3)
                + phone.substring(phone.length() - 2);
    }
}
