package com.demo.practice.service;

public interface OtpService {

	void generateAndSendOtp(String userId, String email);

	boolean verifyOtp(String userId, String otp, String email);
}
