package com.demo.practice.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.demo.practice.entity.User;
import com.demo.practice.entity.UserCredentials;
import com.demo.practice.entity.UserPswdResetToken;
import com.demo.practice.exception.PracticeAppException;
import com.demo.practice.repository.UserRepo;
import com.demo.practice.repository.UserResetTokenReo;

@Service
public class OtpServiceImpl implements OtpService {

	private static final Logger logger = LogManager.getLogger(OtpServiceImpl.class);

	@Value("${user.data.not.exist}")
	String dataNotFoundMsg;

	public EmailService emailService;

	public RedisTemplate<String, String> redisTemplate;

	public UserRepo userRepository;

	public UserResetTokenReo userResetTokenReo;

	public StringEncryptor jasyptStringEncryptor;

	public PasswordEncoder passwordEncoder;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@Autowired
	public OtpServiceImpl(RedisTemplate<String, String> redisTemplate, EmailService emailService,
			UserRepo userRepository, UserResetTokenReo userResetTokenReo,
			@Qualifier("jasyptStringEncryptor") StringEncryptor jasyptStringEncryptor,
			PasswordEncoder passwordEncoder) {
		this.emailService = emailService;
		this.redisTemplate = redisTemplate;
		this.userRepository = userRepository;
		this.userResetTokenReo = userResetTokenReo;
		this.jasyptStringEncryptor = jasyptStringEncryptor;
		this.passwordEncoder = passwordEncoder;
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
			logger.error("Error while generating OTP", e);
			throw new PracticeAppException("Unable to generate OTP at this time. Please try again later.");
		}
	}

	@Override
	public boolean verifyOtp(String userId, String otp, String email, long userPkId) {
		try {
			String expiredTime = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("otp.expired.time", String.class));
			String restPassLink = StringUtils
					.defaultString(cacheManager.getCache("configCache").get("password-reset-link", String.class));

			String cachedOtp = redisTemplate.opsForValue().get("OTP:" + userId);

			if (otp.equals(cachedOtp)) {
				redisTemplate.delete("OTP:" + userId);

				String[] search = { "<serach_str>" };
				String[] replace = { String.valueOf(userId) };

				String subject = "Password reset for User Registration";

				String tokenForResetPwd = emailService.generateTokenForPasswordReset(userId);

				String encodedUserId = jasyptStringEncryptor.encrypt(String.valueOf(userPkId));

				restPassLink = UriComponentsBuilder.fromUriString(restPassLink).queryParam("token", tokenForResetPwd)
						.queryParam("userId", encodedUserId).encode().build().toUriString();

				User userUpdate = userRepository.findById(userPkId).orElseThrow(
						() -> new PracticeAppException(StringUtils.replaceEach(dataNotFoundMsg, search, replace)));

				// Existing token for the user will be deleted before saving new token for
				// password reset, as only one token should be active for a user at a time
				if (userResetTokenReo.existsByUserIdAllIgnoreCase(userPkId)) {

					userResetTokenReo.deleteByUserId(userPkId);
					userResetTokenReo.flush();

				}

				userResetTokenReo.saveAndFlush(new UserPswdResetToken(0l, tokenForResetPwd,
						LocalDateTime.now().plusMinutes(Integer.parseInt(expiredTime)), userUpdate));

				emailService.sendEmail(email, subject, otp, userId, expiredTime, "password-reset.mustache",
						cacheManager, restPassLink);
				return true;
			}
		}

		catch (Exception e) {
			logger.error("Error while verifying OTP", e);
			return false;
		}
		return false;
	}

	@Override
	public boolean resetPassword(String userPkId, String token, String hashCode) {
		try {
			long userPkIdLng = Long.parseLong(jasyptStringEncryptor.decrypt(String.valueOf(userPkId)));

			if (userResetTokenReo.existsByUserIdAllIgnoreCase(userPkIdLng)) {

				boolean isActive = userResetTokenReo.findByUserId(userPkIdLng).stream().anyMatch(
						e -> LocalDateTime.now().isBefore(e.getExpiryTime()) && token.equals(e.getUserToken()));

				if (isActive) {

					String[] search = { "<serach_str>" };
					String[] replace = { String.valueOf(userPkIdLng) };

					String decodedHashCode = URLDecoder.decode(hashCode, StandardCharsets.UTF_8);

					User userUpdate = userRepository.findById(userPkIdLng).orElseThrow(
							() -> new PracticeAppException(StringUtils.replaceEach(dataNotFoundMsg, search, replace)));

					userUpdate.setUserCredentials(new ArrayList<>(
							Arrays.asList(new UserCredentials(userUpdate.getUserCredentials().get(0).getUserCrednid(),
									userUpdate.getUserId(), passwordEncoder.encode(decodedHashCode),
									(userUpdate.getUserCredentials().get(0).getRole()), userUpdate))));

					userRepository.saveAndFlush(userUpdate);

				}
				return isActive;
			}
		} catch (Exception e) {
			logger.error("Error while reset password", e);
			return false;
		}
		return false;
	}

	@Override
	public boolean validLink(String userPkId, String token) {
		try {
			long userPkIdLng = Long.parseLong(jasyptStringEncryptor.decrypt(String.valueOf(userPkId)));

			if (userResetTokenReo.existsByUserIdAllIgnoreCase(userPkIdLng)) {

				return userResetTokenReo.findByUserId(userPkIdLng).stream().anyMatch(
						e -> LocalDateTime.now().isBefore(e.getExpiryTime()) && token.equals(e.getUserToken()));
			}
		} catch (Exception e) {
			logger.error("Error while validating reset password link ", e);
			return false;
		}
		return false;
	}

}
