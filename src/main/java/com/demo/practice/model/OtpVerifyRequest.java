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
public class OtpVerifyRequest {
	private String userId;
	private String otp;
	private String email;
	private long userPkId;

}
