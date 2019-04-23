package ru.saidgadjiev.bibliographya.security.context;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.security.provider.JwtAuthenticationToken;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class JwtSecurityContextRepository implements SecurityContextRepository {

    private AuthTokenService tokenService;

    private JwtProperties jwtProperties;

    private AuthenticationManager authenticationManager;

    public JwtSecurityContextRepository(AuthTokenService tokenService,
                                        JwtProperties jwtProperties) {
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        SecurityContext context = SecurityContextHolder.getContext();

        if (context == null) {
            context = SecurityContextHolder.createEmptyContext();
        }

        if (!CorsUtils.isPreFlightRequest(requestResponseHolder.getRequest())) {
            Cookie tokenCookie = CookieUtils.getCookie(requestResponseHolder.getRequest(), jwtProperties.tokenName());

            //Пробуем по cookie
            if (tokenCookie != null) {
                auth(tokenCookie.getValue(), context);
            } else {
                //Пробуем по header-у
                String token = requestResponseHolder.getRequest().getHeader(jwtProperties.tokenName());

                if (StringUtils.isNotBlank(token) && !"null".equals(token)) {
                    auth(token, context);
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

    private void auth(String token, SecurityContext context) {
        Map<String, Object> claims = tokenService.validate(token);

        try {
            Authentication authentication = authenticationManager.authenticate(new JwtAuthenticationToken(claims));

            context.setAuthentication(authentication);
        } catch (BadCredentialsException ex) {
            context.setAuthentication(null);
        }
    }
}
