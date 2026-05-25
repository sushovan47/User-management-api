package com.demo.practice.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Entity(name = "user_credentials")
public class UserCredentials implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1909104737840199148L;
	@Id
	@Column(name = "user_credn_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userCrednid;
	@Column(name = "login_user_id", unique = true, nullable = false)
	private String userId;
	@Column(name = "hash_code", nullable = false)
	private String hashPwdCode;
	@Column(name = "role")
	private String role;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ToString.Exclude
	@JsonIgnore
	private User user;

//	public UserCredentials(String userId, String role) {
//		this.userId = userId;
//		this.role = role;
//	}

}
