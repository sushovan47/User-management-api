package com.demo.practice.bean;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

//@Configuration
public class MailConfig {

	@Autowired
	@Qualifier("jasyptStringEncryptor")
	StringEncryptor jasyptStringEncryptor;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@Bean
	public JavaMailSender javaMailSender(org.springframework.core.env.Environment env) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(
				StringUtils.defaultString(cacheManager.getCache("configCache").get("spring.mail.host", String.class)));
		mailSender.setPort(Integer.parseInt(
				StringUtils.defaultString(cacheManager.getCache("configCache").get("spring.mail.port", String.class))));
		mailSender.setUsername(StringUtils
				.defaultString(cacheManager.getCache("configCache").get("spring.mail.username", String.class)));

		// 🔑 Decrypt password before setting
		String encryptedPassword = env.getProperty(StringUtils
				.defaultString(cacheManager.getCache("configCache").get("spring.mail.password", String.class)));
		String decryptedPassword = jasyptStringEncryptor.decrypt(encryptedPassword);
		mailSender.setPassword(decryptedPassword);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		return mailSender;
	}
}
