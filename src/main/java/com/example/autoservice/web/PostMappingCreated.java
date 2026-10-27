package com.example.autoservice.web;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Тип 2: композитна (мета-)анотація.
// Об'єднує POST-маппінг і @ResponseStatus(CREATED).
// Увага: мета-анотувати можна лише анотацією, чий @Target включає TYPE (а не METHOD, як у @PostMapping),
// тому тут використано @RequestMapping(method = POST) - з неї й зроблена сама @PostMapping.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
public @interface PostMappingCreated {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
