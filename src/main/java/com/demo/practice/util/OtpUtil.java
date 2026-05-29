package com.demo.practice.util;

import java.security.SecureRandom;

public class OtpUtil {
	private static final SecureRandom random = new SecureRandom();

	public static String generateOtp() {
		int number = 100000 + random.nextInt(900000); // 6 digits OTP
		return String.valueOf(number);
	}

}
