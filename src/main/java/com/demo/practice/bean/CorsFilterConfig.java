package com.demo.practice.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

}