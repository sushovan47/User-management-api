package com.demo.practice.bean;

import java.io.IOException;
import java.util.Arrays;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.demo.practice.util.ApplicationConstant;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Component
public class CorsFilterConfig {
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // Applies to all paths
						.allowedOrigins("*") // Allows any origin
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allows all methods
						.allowedHeaders("*") // Allows all headers
						.allowCredentials(false); // Must be false if allowedOrigins is "*"
			}
		};
	}

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

	@Bean(name = "openAPIBean")
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		return new OpenAPI().info(new Info().title("Latest Practice API").version("1.0"))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components().addSecuritySchemes(securitySchemeName,
						new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
								.bearerFormat("JWT")
								.description("Suggestion: Paste your JWT token here. Example: eyJhbGciOiJIUzI1Ni...")));
	}

}