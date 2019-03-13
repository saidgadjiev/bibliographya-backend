package ru.saidgadjiev.bibliographya.security.context;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsUtils;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.security.provider.JwtAuthenticationToken;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JwtSecurityContextRepository implements SecurityContextRepository {

    private TokenService tokenService;

    private JwtProperties jwtProperties;

    private AuthenticationManager authenticationManager;

    public JwtSecurityContextRepository(TokenService tokenService,
                                        JwtProperties jwtProperties,
                                        AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        SecurityContext context = SecurityContextHolder.getContext();

        if (context == null) {
            context = SecurityContextHolder.createEmptyContext();
        }

        if (!CorsUtils.isPreFlightRequest(requestResponseHolder.getRequest())) {
            Cookie tokenCookie = CookieUtils.getCookie(requestResponseHolder.getRequest(), jwtProperties.cookieName());

            if (tokenCookie != null) {
                Map<String, Object> claims = tokenService.validate(tokenCookie.getValue());

                try {
                    Authentication authentication = authenticationManager.authenticate(new JwtAuthenticationToken(claims));

                    context.setAuthentication(authentication);
                } catch (BadCredentialsException ex) {
                    context.setAuthentication(null);
                }
            }
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }
}
