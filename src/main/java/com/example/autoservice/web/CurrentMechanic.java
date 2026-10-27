package com.example.autoservice.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Тип 3: анотація для кастомного HandlerMethodArgumentResolver.
// Параметр контролера, позначений нею, автоматично отримує ім'я механіка із заголовка X-Mechanic.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMechanic {
}
