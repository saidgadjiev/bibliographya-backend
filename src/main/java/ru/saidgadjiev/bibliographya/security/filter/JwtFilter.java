package ru.saidgadjiev.bibliographya.security.filter;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.saidgadjiev.bibliographya.service.impl.auth.AuthService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 25.01.2019.
 */
public class JwtFilter extends OncePerRequestFilter {

    private AuthService authService;

    public JwtFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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

        filterChain.doFilter(request, response);
    }
}
