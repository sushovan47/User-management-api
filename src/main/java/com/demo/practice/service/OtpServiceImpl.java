package com.demo.practice.service;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.demo.practice.exception.PracticeAppException;

@Service
public class OtpServiceImpl implements OtpService {

	public EmailService emailService;

	public RedisTemplate<String, String> redisTemplate;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@Autowired
	public OtpServiceImpl(RedisTemplate<String, String> redisTemplate, EmailService emailService) {
		this.emailService = emailService;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void generateAndSendOtp(String userId, String email) {
		try {
			String expiredTime = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("otp.expired.time", String.class));
			String restPassLink = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("password-reset-link", String.class));

			String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // Generate a 6-digit OTP

			redisTemplate.opsForValue().set("OTP:" + userId, otp, Duration.ofMinutes(Integer.parseInt(expiredTime)));

			String subject = "Your OTP Code for User Registration";
			emailService.sendEmail(email, subject, otp, userId, expiredTime, "otp-email.mustache", cacheManager,
					restPassLink);
		}

		catch (Exception e) {
			System.err.println("Redis unavailable while saving OTP: " + e.getMessage());
			throw new PracticeAppException("Unable to generate OTP at this time. Please try again later.");
		}
	}

	@Override
	public boolean verifyOtp(String userId, String otp, String email) {
		try {
			String expiredTime = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("otp.expired.time", String.class));
			String restPassLink = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("password-reset-link", String.class));
			String cachedOtp = redisTemplate.opsForValue().get("OTP:" + userId);
			if (otp.equals(cachedOtp)) {
				redisTemplate.delete("OTP:" + userId);
				String subject = "Password reset for User Registration";
				emailService.sendEmail(email, subject, otp, userId, expiredTime, "password-reset.mustache",
						cacheManager, restPassLink);
				return true;
			}

		}

		catch (Exception e) {
			System.err.println("Redis unavailable while verifying OTP: " + e.getMessage());
			return false;
		}
		return false;
	}

}
