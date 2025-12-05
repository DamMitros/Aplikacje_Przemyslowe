package com.example.zad1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TechCorpEmailValidator implements ConstraintValidator<TechCorpEmail, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){
        if (value==null) {
            return true;
        }
        return value.endsWith("@techcorp.com");
    }
}
