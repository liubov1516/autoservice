package com.example.autoservice.web;

import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// Реєструє CurrentMechanicArgumentResolver у Spring MVC
@Component
public class WebConfig implements WebMvcConfigurer {

    private final CurrentMechanicArgumentResolver currentMechanicArgumentResolver;

    public WebConfig(CurrentMechanicArgumentResolver currentMechanicArgumentResolver) {
        this.currentMechanicArgumentResolver = currentMechanicArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMechanicArgumentResolver);
    }
}
