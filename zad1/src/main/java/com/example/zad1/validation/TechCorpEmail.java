package com.example.zad1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TechCorpEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface TechCorpEmail {
    String message() default "Email musi znajdować się w domenie @techcorp.com";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
