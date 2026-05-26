package com.demo.practice.bean;

import java.time.Duration;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean(name = "localCacheManager")
	@Primary
	public CacheManager localCacheManager() {
		return new ConcurrentMapCacheManager("configCache");
	}

	@Bean(name = "redisCacheManager")
	public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(2));
		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).initialCacheNames(Set.of("otpCache"))
				.build();
	}

}
