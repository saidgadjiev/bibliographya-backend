package ru.saidgadjiev.bibliographya.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    private CookieUtils() { }


    public static void addCookie(HttpServletRequest request, HttpServletResponse response, String tokenName, String token) {
        Cookie cookie = new Cookie(tokenName, token);

        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String tokenName) {
        Cookie cookie = new Cookie(tokenName, null);

        cookie.setDomain(request.getServerName());
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
