package com.demo.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class PracticeAppException extends RuntimeException {

	private static final long serialVersionUID = 4534267339122577257L;

	public PracticeAppException(String message) {
		super(message);

	}

}
