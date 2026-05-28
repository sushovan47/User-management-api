package com.demo.practice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@DynamicUpdate
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = { "email", "user_id" }))
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private long id;
	@Column(name = "first_name", nullable = false)
	private String firstName;
	@Column(name = "last_name", nullable = false)
	private String lastName;
	@Column(name = "email", unique = true, nullable = false)
	private String email;
	@Column(name = "mobile", unique = true, nullable = false)
	private String mobileNo;
	@Column(name = "dob", nullable = false)
	private LocalDate dob;
	@Column(name = "gender")
	private String gender;
	@Column(name = "login_user_id", unique = true)
	private String userId;
	@Column(name = "updated_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = true)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private LocalDateTime updatedOn = LocalDateTime.now();
	@Column(name = "updated_by")
	private String updatedBy;
	@Column(name = "is_active", nullable = false)
	@JsonProperty("active")
	private boolean isActive;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
	@ToString.Exclude
	private List<UserCredentials> userCredentials;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
	@ToString.Exclude
	private List<UserPswdResetToken> userPswdResetToken;

}
