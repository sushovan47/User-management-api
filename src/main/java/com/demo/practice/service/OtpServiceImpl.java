package com.demo.practice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {

	public EmailService emailService;

	public CacheManager redisCacheManager;

	@Autowired
	public OtpServiceImpl(RedisTemplate<String, String> redisTemplate, EmailService emailService,
			@Qualifier("redisCacheManager") CacheManager redisCacheManager) {
		this.redisCacheManager = redisCacheManager;
		this.emailService = emailService;
	}

	@Override
	public void generateAndSendOtp(String userId, String email) {
		String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // Generate a 6-digit OTP
		Cache cache = redisCacheManager.getCache("otpCache");
        if (cache != null) {
        	cache.put("OTP:" + userId, otp); // Store OTP in Redis cache with userId as key
        }
//		redisTemplate.opsForValue().set("OTP:" + userId, otp, Duration.ofMinutes(2)); // Store OTP in Redis with userId
																						// as key duration 2 minutes.
		String subject = "Your OTP Code for User Registration";
		String body = "Your OTP code is: " + otp;
		emailService.sendEmail(email, subject, body); // Send OTP via email
	}

	@Override
	public boolean verifyOtp(String userId, String otp) {
		Cache cache = redisCacheManager.getCache("otpCache");
		if (cache != null) {
			String cachedOtp = cache.get("OTP:" + userId, String.class); // Retrieve OTP from Redis cache
			return cachedOtp != null && cachedOtp.equals(otp);
		}
		return false;
	}

}
