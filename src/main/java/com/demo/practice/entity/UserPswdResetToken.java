package com.demo.practice.entity;

import java.time.LocalDateTime;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "user_pswd_reset_token")
@Audited
@AuditTable("user_pswd_reset_token_history")
public class UserPswdResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_pswd_reset_token_id")
	private long userPswdResetTokenId;

	@Column(name = "token", nullable = false, unique = true)
	private String userToken;

	@Column(name = "expiry_time", nullable = false)
	private LocalDateTime expiryTime;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ToString.Exclude
	@JsonIgnore
	private User user;

}
