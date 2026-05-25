package com.demo.practice.bean;

import java.io.IOException;
import java.util.Arrays;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;

import com.demo.practice.util.ApplicationConstant;

@Configuration
public class AppConfig {

	@Value("classpath:latest-code-app-private-key.txt")
	private Resource resourcePrivateKey;

	@Bean(name = "jasyptStringEncryptor")
	public StringEncryptor getPassEncryptor() throws IOException {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(getPrivateKey());
		config.setAlgorithm(ApplicationConstant.JASYPT_ALGO);
		config.setKeyObtentionIterations(ApplicationConstant.JASYPT_KEY_OBTENTION);
		config.setPoolSize(ApplicationConstant.JASYPT_POOL_SIZE);
		config.setProviderName(ApplicationConstant.JASYPT_PROVIDER_NAME);
		config.setSaltGeneratorClassName(ApplicationConstant.JASYPT_SALT_GEN_CLASS);
		config.setStringOutputType(ApplicationConstant.JASYPT_OPTYPE);
		encryptor.setConfig(config);
		return encryptor;
	}

	private String getPrivateKey() throws IOException {
		return Arrays.toString(resourcePrivateKey.getInputStream().readAllBytes());
	}
}
