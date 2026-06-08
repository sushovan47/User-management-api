package com.demo.practice.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.practice.entity.User;
import com.demo.practice.exception.PracticeAppException;
import com.demo.practice.repository.UserRepo;

@Service
public class AdminServiceImpl implements AdminService {

	private static final Logger logger = LogManager.getLogger(AdminServiceImpl.class);

	@Autowired
	UserRepo userRepository;

	@Override
	public List<User> fetchAllUserList() {
		return userRepository.findAll();
	}

	@Override
	public Long deleteUser(long id) {
		try {
			userRepository.deleteById(id);
			return id;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new PracticeAppException(e.getMessage());
		}
	}

	@Override
	public List<User> fetchUserById(String searchParamKey) {
		// TODO Auto-generated method stub
		return null;
	}

}
