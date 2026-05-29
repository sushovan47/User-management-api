package com.demo.practice.bean;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
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

}
