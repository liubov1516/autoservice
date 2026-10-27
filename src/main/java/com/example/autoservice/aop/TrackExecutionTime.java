package com.example.autoservice.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Анотація для аспекту вимірювання часу виконання методу (Around advice)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecutionTime {

    // поріг у мілісекундах, вище якого пишемо попередження в лог
    long warnThresholdMs() default 200;
}
