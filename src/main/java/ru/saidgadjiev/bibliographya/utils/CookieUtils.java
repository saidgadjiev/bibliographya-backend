package ru.saidgadjiev.bibliographya.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    private CookieUtils() { }


    public static void addCookie(HttpServletResponse response, String serverName, String tokenName, String token) {
        Cookie cookie = new Cookie(tokenName, token);

        cookie.setDomain(serverName);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 30); // one month
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletResponse response, String serverName, String tokenName) {
        Cookie cookie = new Cookie(tokenName, null);

        cookie.setDomain(serverName);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }

        return null;
    }
}
