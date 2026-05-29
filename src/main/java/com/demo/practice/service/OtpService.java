package com.demo.practice.service;

public interface OtpService {

	void generateAndSendOtp(String userId, String email);

	boolean verifyOtp(String userId, String otp, String email, long userPkId);

	boolean resetPassword(String userPkId, String token, String hashCode);

	boolean validLink(String userPkId, String token);
}
