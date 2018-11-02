package ru.saidgadjiev.bibliography.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ru.saidgadjiev.bibliography.interceptor.CrossInterceptor;
import ru.saidgadjiev.bibliography.interceptor.JwtInterceptor;
import ru.saidgadjiev.bibliography.service.api.TokenService;

/**
 * Created by said on 29.10.2018.
 */
@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    private final TokenService tokenService;

    private final UserDetailsService userDetailsService;

    @Autowired
    public WebMvcConfigurer(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CrossInterceptor());
        registry.addInterceptor(new JwtInterceptor(tokenService, userDetailsService));
    }
}
