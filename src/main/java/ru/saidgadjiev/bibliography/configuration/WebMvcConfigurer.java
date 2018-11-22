package ru.saidgadjiev.bibliography.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ru.saidgadjiev.bibliography.interceptor.CrossInterceptor;
import ru.saidgadjiev.bibliography.interceptor.JwtInterceptor;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.api.TokenService;

import java.util.List;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return OffsetLimitPageRequest.class.equals(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                return new OffsetLimitPageRequest.Builder()
                        .setLimit(Integer.parseInt(webRequest.getParameter("limit")))
                        .setOffset(Integer.parseInt(webRequest.getParameter("offset"))).build();
            }
        });
    }
}
