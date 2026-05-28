package com.demo.practice.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.demo.practice.exception.PracticeAppException;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	private static final Logger logger = LogManager.getLogger(EmailService.class);

	public StringEncryptor jasyptStringEncryptor;

	public Mustache.Compiler mustacheCompiler;

	public ResourceLoader resourceLoader;

	public EmailService(Mustache.Compiler mustacheCompiler, ResourceLoader resourceLoader,
			@Qualifier("jasyptStringEncryptor") StringEncryptor jasyptStringEncryptor) {
		this.mustacheCompiler = mustacheCompiler;
		this.resourceLoader = resourceLoader;
		this.jasyptStringEncryptor = jasyptStringEncryptor;
	}

	private static final String resourcePath = "classpath:templates/";

	private JavaMailSenderImpl getCachedMailSender(CacheManager cacheManager) {

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(
				StringUtils.defaultString(cacheManager.getCache("configCache").get("spring.mail.host", String.class)));
		mailSender.setPort(Integer.parseInt(
				StringUtils.defaultString(cacheManager.getCache("configCache").get("spring.mail.port", String.class))));
		mailSender.setUsername(StringUtils
				.defaultString(cacheManager.getCache("configCache").get("spring.mail.username", String.class)));

		String encryptedPassword = StringUtils.defaultString(
				StringUtils.replaceEach(cacheManager.getCache("configCache").get("spring.mail.password", String.class),
						new String[] { "ENC(", ")" }, new String[] { "", "" }));
		mailSender.setPassword(jasyptStringEncryptor.decrypt(encryptedPassword));

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);

		return mailSender;
	}

//	@Async
	public void sendEmail(String toEmail, String subject, String otp, String userId, String expiredTime,
			String resourceName, CacheManager cacheManager, String resetPasswordLink)
			throws PracticeAppException, Exception {
		try {
			JavaMailSenderImpl dynamicMailSender = getCachedMailSender(cacheManager);
			MimeMessage message = dynamicMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject(subject);

			// 1. Prepare dynamic template data using a simple Map
			Map<String, String> templateData = new HashMap<>();
			templateData.put("companyName", cacheManager.getCache("configCache").get("app-name", String.class));
			templateData.put("userName", userId);
			templateData.put("otpCode", otp);
			templateData.put("expiryTime", expiredTime);
			templateData.put("currentYear", String.valueOf(Year.now().getValue()));
			templateData.put("resetLink", resetPasswordLink);

			Resource resource = resourceLoader.getResource(resourcePath + resourceName);
			try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
				Template template = mustacheCompiler.compile(reader);

				String finalHtml = template.execute(templateData);
				helper.setText(finalHtml, true);
			}
			dynamicMailSender.send(message);

		} catch (Exception e) {
			logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
			throw new PracticeAppException("Failed to send email: " + e.getMessage());
		}
	}

	public String generateTokenForPasswordReset(String userId) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest
					.digest((userId + UUID.randomUUID().toString().replace("-", "")).getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new PracticeAppException("Error while encrypting for reset password", e);
		}

	}

}
