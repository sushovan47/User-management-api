package com.demo.practice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.practice.entity.UserPswdResetToken;

import jakarta.transaction.Transactional;

public interface UserResetTokenReo extends JpaRepository<UserPswdResetToken, Long> {

	boolean existsByUserIdAllIgnoreCase(long userId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM user_pswd_reset_token t WHERE t.user_id = :userId", nativeQuery = true)
	void deleteByUserId(@Param("userId") Long userId);

	List<UserPswdResetToken> findByUserId(long userId);

}
