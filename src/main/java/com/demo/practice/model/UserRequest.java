package com.demo.practice.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.demo.practice.bean.CustomValidation;
import com.demo.practice.bean.ValidationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1909104737840199148L;

	// Marker interface for create operations
	public interface OnCreate {
	}

	// Marker interface for update operations
	public interface OnUpdate {
	}

	private long id;
	@NotBlank(message = "{user.firstname.required}")
	private String firstName;
	@NotBlank(message = "{user.lastname.required}")
	private String lastName;
	@NotBlank(message = "{user.email.required}")
	@Email(message = "{user.email.valid}", regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE)
	private String email;
	@NotBlank(message = "{user.dob.required}")
	@CustomValidation(message = "{user.dob.valid}", type = ValidationType.DOB)
	private String dob;
	@NotBlank(message = "{user.mobile.required}")
	private String mobileNo;
	@NotBlank(message = "{user.userid.required}")
	private String userId;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private LocalDateTime updatedOn = LocalDateTime.now();
	private String updatedBy;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	@JsonProperty("active")
	private boolean isActive;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	@JsonProperty("isAdmin")
	private boolean isAdmin;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	@JsonProperty("isUser")
	private boolean isUser;
	@NotBlank(message = "{user.haschode.required}", groups = OnCreate.class)
	private String hashPwdCode;
	private String gender;

}
