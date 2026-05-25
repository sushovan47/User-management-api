package com.demo.practice.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = CustomValidationImpl.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomValidation {
	String message() default "Invalid value provided";
	ValidationType type(); // enum to specify DOB, PREFIX, etc
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
