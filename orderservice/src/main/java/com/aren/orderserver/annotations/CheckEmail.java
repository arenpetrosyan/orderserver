package com.aren.orderserver.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface CheckEmail {

    String value() default "";
    String message() default "Email must be a valid email address";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
