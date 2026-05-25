package com.demo.practice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AuthResponse {

	private String token;
	private String tokenType;
	private String expirationTime;
	private String message;
	private String userRole;
	private String userId;
	private boolean success;

}
