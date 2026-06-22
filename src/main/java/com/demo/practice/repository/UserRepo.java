package com.demo.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.practice.entity.User;
import com.demo.practice.model.AuditLogDto;

public interface UserRepo extends JpaRepository<User, Long>, CustomAuditRepository {

	boolean existsByEmailOrUserIdAllIgnoreCase(String email, String userId);

	List<User> findByUserIdContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(String userId,
			String firstName, String lastName, String email);

	Optional<User> findByUserId(String userId);

	boolean existsByEmailAllIgnoreCase(String email);

	boolean existsByUserIdAllIgnoreCase(String userId);

	List<User> findEmailByUserId(String userId);
}

interface CustomAuditRepository {
	List<AuditLogDto> getUserHistoryLog(Long userId);
}
