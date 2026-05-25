package com.demo.practice.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalPracticeAppHandler {

	@ExceptionHandler(PracticeAppException.class)
	public ResponseEntity<Object> handleAppException(PracticeAppException ex) {

		Map<String, Object> returnBody = new LinkedHashMap<>();
		returnBody.put("timestamp", LocalDateTime.now());
		returnBody.put("message", ex.getMessage());
		returnBody.put("isSuccess", false);
		returnBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

		return new ResponseEntity<>(returnBody, HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
