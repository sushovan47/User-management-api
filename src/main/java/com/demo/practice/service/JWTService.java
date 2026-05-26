package com.demo.practice.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

	private final CacheManager cacheManager;

	public JWTService(@Qualifier("localCacheManager") CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(
				StringUtils.defaultString(cacheManager.getCache("configCache").get("app.jwt.secret", String.class))
						.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UserDetails userDetails) {
		return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + Long.valueOf(StringUtils
						.defaultString(cacheManager.getCache("configCache").get("app.jwt.expiration", String.class)))))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String extractUsername(String token) {
		Claims claims = getClaims(token);
		return claims != null ? getClaims(token).getSubject() : null;
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return getClaims(token).getExpiration().before(new Date(0));
	}

	private Claims getClaims(String token) {
		Claims claims = null;
		try {
			claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
		} catch (Exception e) {
			return null;
		}
		return claims;
	}

	public String getExpirationTime() {
		return StringUtils.defaultString(cacheManager.getCache("configCache").get("app.jwt.expiration", String.class));
	}

}
