package ru.saidgadjiev.bibliographya.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.http.message.AuthKeyMessageConverter;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.util.List;

/**
 * Created by said on 29.10.2018.
 */
@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    private final SortArgumentResolver sortArgumentResolver;

    @Autowired
    public WebMvcConfigurer(SortArgumentResolver sortArgumentResolver) {
        this.sortArgumentResolver = sortArgumentResolver;
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
                        .setSort(sortArgumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory));

                return builder.build();
            }
        });

        resolvers.add(new AuthKeyArgumentResolver());
    }

    @Bean
    public HttpMessageConverter<?> verificationKeyConverter(ObjectMapper objectMapper) {
        return new AuthKeyMessageConverter(objectMapper);
    }
}
