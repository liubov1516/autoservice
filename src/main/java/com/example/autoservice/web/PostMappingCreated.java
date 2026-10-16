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
// Об'єднує POST-маппінг і @ResponseStatus(CREATED), які в коді завжди йдуть разом
// для ендпоінтів створення ресурсу - той самий підхід, що й у @SpringBootApplication.
//
// Важливо: тут не можна мета-анотувати через саму @PostMapping, бо її @Target = METHOD,
// а методом-міткою не можна позначати оголошення ІНШОЇ анотації (потрібен @Target(TYPE)).
// Тому використовуємо @RequestMapping(method = POST) - те саме, з чого всередині
// Spring зроблена і сама @PostMapping, але її @Target уже включає TYPE.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
public @interface PostMappingCreated {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
