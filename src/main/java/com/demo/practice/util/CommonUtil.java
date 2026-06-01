package com.demo.practice.util;

import java.security.SecureRandom;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CommonUtil {
	@Autowired
	@Qualifier("validationMap")
	private Map<String, String> validationMap;

	private static final SecureRandom random = new SecureRandom();

	public static String generateOtp() {
		int number = 100000 + random.nextInt(900000); // 6 digits OTP
		return String.valueOf(number);
	}

	public String getValidationMessage(String key) {
		return validationMap.getOrDefault(key, "Error: Key [" + key + "] not found in map");
	}

}
