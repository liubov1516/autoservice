package com.example.autoservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Тип 1: власна Bean Validation constraint-анотація.
// Перевіряє, що державний номер авто відповідає українському формату: АА1234АА.
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlateNumberValidator.class)
public @interface ValidPlateNumber {

    String message() default "Невірний формат державного номера (приклад: AA1234BB)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
