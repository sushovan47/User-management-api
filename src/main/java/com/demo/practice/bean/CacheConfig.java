package com.demo.practice.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

@Configuration
@EnableCaching
public class CacheConfig {

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private RedisServer redisServer;

	@PostConstruct
	public void startRedis() throws IOException {
		// Initializes the embedded Redis server on the configured port
		this.redisServer = new RedisServer(redisPort);
		this.redisServer.start();
		System.out.println(">>>>>>>>>>>> Embedded Redis started successfully on port<<<<<<<<<<<<: " + redisPort);
	}

	@PreDestroy
	public void stopRedis() throws IOException {
		if (this.redisServer != null) {
			this.redisServer.stop();
			System.out.println(">>>>>>>>>>>>>>> Embedded Redis stopped smoothly. <<<<<<<<<<<<<<<<<<");
		}
	}

	@Bean(name = "localCacheManager")
	@Primary
	public CacheManager localCacheManager() {
		return new ConcurrentMapCacheManager("configCache");
	}

	@Bean(name = "validationMap")
	public Map<String, String> validationMap() {
		Map<String, String> map = new HashMap<>();
		try {
			ClassPathResource resource = new ClassPathResource("validationmessages.properties");
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);

			for (String key : properties.stringPropertyNames()) {
				map.put(key, properties.getProperty(key));
			}

			System.out.println("Successfully loaded validation map. Total keys: " + map.size());
		} catch (IOException e) {
			System.err.println("CRITICAL: Could not find or read validationmessages.properties file!");
			e.printStackTrace();
		}
		return map;
	}

}
