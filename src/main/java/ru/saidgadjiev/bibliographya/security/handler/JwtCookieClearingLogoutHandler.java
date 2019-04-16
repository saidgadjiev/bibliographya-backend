package ru.saidgadjiev.bibliographya.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtCookieClearingLogoutHandler implements LogoutHandler {

    private UIProperties uiProperties;
    private final JwtProperties jwtProperties;

    public JwtCookieClearingLogoutHandler(UIProperties uiProperties, JwtProperties jwtProperties) {
        this.uiProperties = uiProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie cookie = new Cookie(jwtProperties.tokenName(), null);

        cookie.setDomain(uiProperties.getHost());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
