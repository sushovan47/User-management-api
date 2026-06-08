package com.demo.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.practice.entity.UserCredentials;

public interface UserCredentialsRepo extends JpaRepository<UserCredentials, Long> {

	UserCredentials findByUserId(long userId);

}
