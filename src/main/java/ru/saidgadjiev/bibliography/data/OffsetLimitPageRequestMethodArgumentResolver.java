package ru.saidgadjiev.bibliography.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;

/**
 * Created by said on 20.11.2018.
 */
public class OffsetLimitPageRequestMethodArgumentResolver implements PageableArgumentResolver {

    private final SortArgumentResolver sortArgumentResolver;

    public OffsetLimitPageRequestMethodArgumentResolver(SortArgumentResolver sortArgumentResolver) {
        this.sortArgumentResolver = sortArgumentResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(OffsetLimitPageRequest.class);
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        return new OffsetLimitPageRequest.Builder()
                .setOffset(Integer.parseInt(nativeWebRequest.getParameter("offset")))
                .setLimit(Integer.parseInt(nativeWebRequest.getParameter("limit")))
                .setSort(sortArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory))
                .build();
    }
}
