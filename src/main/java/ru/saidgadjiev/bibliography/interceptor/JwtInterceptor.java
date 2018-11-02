package ru.saidgadjiev.bibliography.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ru.saidgadjiev.bibliography.service.api.TokenService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by said on 29.10.2018.
 */
public class JwtInterceptor extends HandlerInterceptorAdapter {

    private final TokenService tokenService;

    private final UserDetailsService userDetailsService;

    public JwtInterceptor(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
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
                Map<String, Object> payload = tokenService.validate(tokenCookie.getValue());

                if (payload == null) {
                    SecurityContextHolder.getContext().setAuthentication(null);
                } else {
                    String username = (String) payload.get("username");
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {
                        ((CredentialsContainer) userDetails).eraseCredentials();

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    } else {
                        SecurityContextHolder.getContext().setAuthentication(null);
                    }
                }
            } else {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }

        return true;
    }
}
