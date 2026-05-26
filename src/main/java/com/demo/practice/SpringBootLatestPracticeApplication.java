package com.demo.practice;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableEncryptableProperties
@PropertySource("ValidationMessages.properties")
@EnableCaching
public class SpringBootLatestPracticeApplication {

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootLatestPracticeApplication.class, args);
	}

	@Bean(name = "appLoadMessageBean")
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println(
					Arrays.asList(this.environment.getActiveProfiles()) + " APPLICATION STARTED SUCCESSFULLLY!!!");

		};
	}

	@Bean(name = "initCacheBean")
	public CommandLineRunner initCache(JdbcTemplate jdbcTemplate, CacheManager cacheManager) {
		return args -> {
			Cache cache = cacheManager.getCache("configCache");
			if (cache != null) {
				String sql = "SELECT * FROM app_config";
				jdbcTemplate.queryForList(sql).forEach(row -> {
					cache.put(row.get("conf_key"), row.get("conf_value"));
				});
				System.out.println("Cache populated from DB!");
			}
		};
	}

}
