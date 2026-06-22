package com.demo.practice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PwdRequest {

	@NotNull(message = "{user.pkId.required}")
	private long userPkId;
	private String userId;
	@NotBlank(message = "{user.oldpwd.required}")
	private String oldPassword;
	@NotBlank(message = "{user.newpwd.required}")
	private String newPassword;

}
