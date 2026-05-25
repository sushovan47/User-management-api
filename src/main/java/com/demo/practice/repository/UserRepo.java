package com.demo.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.practice.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {

	boolean existsByEmailOrUserIdAllIgnoreCase(String email, String userId);

	List<User> findByUserIdContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(String userId,
			String firstName, String lastName, String email);

	Optional<User> findByUserId(String userId);
	
	boolean existsByEmailAllIgnoreCase(String email);
	
	boolean existsByUserIdAllIgnoreCase(String userId);

}
