package com.demo.practice.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomValidationImpl implements ConstraintValidator<CustomValidation, String> {

	private ValidationType type;

	@Override
	public void initialize(CustomValidation constraintAnnotation) {
		this.type = constraintAnnotation.type();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		switch (type) {
		case DOB:
			return isValidDob(value);
		case UPDATE:
			return isPasswordMand(value);
		case PREFIX:
			return value.startsWith("PREFIX_");
		default:
			return false;
		}
	}

	private boolean isValidDob(String value) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			sdf.setLenient(false);
			sdf.parse(value);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	private boolean isPasswordMand(String value) {
		if (StringUtils.isEmpty(value)) {
			return true;
		} else {
			return false;
		}
	}

}
