package com.unvus.firo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FiroRoom {
    String value();

    String keyFieldName() default "id";

    String dateFieldName() default "createdDt";
}
