package ru.saidgadjiev.bibliography.configuration;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ru.saidgadjiev.bibliography.interceptor.JwtInterceptor;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthService;

import java.util.List;

/**
 * Created by said on 29.10.2018.
 */
@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    private final AuthService authService;

    private final SortArgumentResolver sortArgumentResolver;

    @Autowired
    public WebMvcConfigurer(AuthService authService, SortArgumentResolver sortArgumentResolver) {
        this.authService = authService;
        this.sortArgumentResolver = sortArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(authService));
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
                OffsetLimitPageRequest.Builder builder = new OffsetLimitPageRequest.Builder();
                String limit = webRequest.getParameter("limit");

                if (StringUtils.isBlank(limit)) {
                    throw new IllegalArgumentException("Limit can't be null");
                }
                String offset = webRequest.getParameter("offset");

                if (StringUtils.isBlank(offset)) {
                    throw new IllegalArgumentException("Offset can't be null");
                }

                builder.setLimit(Integer.parseInt(limit))
                        .setOffset(Long.parseLong(offset))
                        .setSort(sortArgumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                        .setEndAt(Integer.parseInt(webRequest.getParameter("endAt")));

                return builder.build();
            }
        });
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
