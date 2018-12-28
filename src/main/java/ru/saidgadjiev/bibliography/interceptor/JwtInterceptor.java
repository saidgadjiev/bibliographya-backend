package ru.saidgadjiev.bibliography.interceptor;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by said on 29.10.2018.
 */
public class JwtInterceptor extends HandlerInterceptorAdapter {

    private final AuthService authService;

    public JwtInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!HttpMethod.OPTIONS.matches(request.getMethod())) {
            Cookie[] cookies = request.getCookies();
            Cookie tokenCookie = null;

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("X-TOKEN")) {
                        tokenCookie = cookie;
                        break;
                    }
                }
            }

            if (tokenCookie != null) {
                authService.tokenAuth(tokenCookie.getValue());
            } else {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }

        return true;
    }
}
