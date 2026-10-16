package com.example.autoservice.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// Резолвер для @CurrentMechanic: дістає ім'я механіка із заголовка X-Mechanic
@Component
public class CurrentMechanicArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_NAME = "X-Mechanic";
    private static final String DEFAULT_VALUE = "unknown";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMechanic.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String mechanic = webRequest.getHeader(HEADER_NAME);
        if (mechanic == null || mechanic.isBlank()) {
            return DEFAULT_VALUE;
        }
        return mechanic;
    }
}
