package ru.saidgadjiev.bibliographya.utils;

/**
 * Created by said on 11.02.2019.
 */
public class TimeUtils {

    private TimeUtils() { }

    public static boolean isExpired(Long expireTime) {
        return expireTime != null && System.currentTimeMillis() >= expireTime;
    }
}
