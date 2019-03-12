package ru.saidgadjiev.bibliographya.security.filter;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.security.provider.JwtAuthenticationToken;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by said on 25.01.2019.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final JwtProperties jwtProperties;

    private final AuthenticationProvider authenticationProvider;

    public JwtFilter(TokenService tokenService, JwtProperties jwtProperties, AuthenticationProvider authenticationProvider) {
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
        this.authenticationProvider = authenticationProvider;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!HttpMethod.OPTIONS.matches(request.getMethod())) {
            Cookie tokenCookie = CookieUtils.getCookie(request, jwtProperties.cookieName());

            if (tokenCookie != null) {
                Map<String, Object> claims = tokenService.validate(tokenCookie.getValue());

                try {
                    Authentication authentication = authenticationProvider.authenticate(new JwtAuthenticationToken(claims));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (BadCredentialsException ex) {
                    SecurityContextHolder.getContext().setAuthentication(null);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
