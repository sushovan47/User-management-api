package com.demo.practice.service;

import java.util.List;

import com.demo.practice.entity.User;

public interface AdminService {

	List<User> fetchAllUserList();

	Long deleteUser(long id);

	List<User> fetchUserById(String searchParamKey);

}
