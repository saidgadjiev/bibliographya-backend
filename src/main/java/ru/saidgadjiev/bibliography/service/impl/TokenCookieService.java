package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by said on 25.10.2018.
 */
@Service
public class TokenCookieService {

    public void addCookie(HttpServletResponse response, String tokenName, String token) {
        Cookie cookie = new Cookie(tokenName, token);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response, String tokenName) {
        Cookie cookie = new Cookie(tokenName, null);

        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public String getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        Cookie tokenCookie = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
