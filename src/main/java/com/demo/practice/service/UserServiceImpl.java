package com.demo.practice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.practice.entity.User;
import com.demo.practice.entity.UserCredentials;
import com.demo.practice.exception.PracticeAppException;
import com.demo.practice.model.UserRequest;
import com.demo.practice.repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {

	@Value("${user.email.userid.exist}")
	String inValidMailMsg;

	@Value("${user.data.not.exist}")
	String dataNotFoundMsg;

	@Autowired
	UserRepo userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<User> fetchAllUserList() {
		return userRepository.findAll();
	}

	@Override
	public List<User> fetchUserById(String searchParamKey) {
		return userRepository.findByUserIdContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(
				searchParamKey, searchParamKey, searchParamKey, searchParamKey);
	}

	@Override
	public Long saveUser(UserRequest userRequest) {
		if (userRepository.existsByEmailAllIgnoreCase(userRequest.getEmail())) {
			throw new PracticeAppException("User with this email already exist. Please try with different email.");
		}
		if (userRepository.existsByUserIdAllIgnoreCase(userRequest.getUserId())) {
			throw new PracticeAppException("User with this userId already exist. Please try with different userId.");
		}
//		if (!userRepository.existsByEmailOrUserIdAllIgnoreCase(userRequest.getEmail(), userRequest.getUserId())
//				&& !userRepository.findByUserId(userRequest.getUserId()).isPresent()) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate dob = LocalDate.parse(userRequest.getDob(), formatter);

		User user = new User();
		user.setDob(dob);
		user.setEmail(userRequest.getEmail());
		user.setMobileNo(userRequest.getMobileNo());
		user.setGender(userRequest.getGender());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setUserId(userRequest.getUserId());
		user.setActive(true);
		user.setUpdatedBy(userRequest.getUserId());
		user.setId(0l);
		user.setUserCredentials(List.of(new UserCredentials(0l, userRequest.getUserId(),
				passwordEncoder.encode(userRequest.getHashPwdCode()),
				(userRequest.isAdmin() ? "ROLE_ADMIN" : userRequest.isUser() ? "ROLE_USER" : "ROLE_USER"), user)));

		if (userRepository.save(user) != null) {
			return user.getId();
		}
//		}
		return 0l;
	}

	@Override
	public long updateUser(UserRequest userRequest, long id) {
		try {
			String[] search = { "<serach_str>" };
			String[] replace = { String.valueOf(id) };

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			LocalDate dob = LocalDate.parse(userRequest.getDob(), formatter);

			User userUpdate = userRepository.findById(id).orElseThrow(
					() -> new PracticeAppException(StringUtils.replaceEach(dataNotFoundMsg, search, replace)));
			LocalDateTime beforeSave = userUpdate.getUpdatedOn();
			List.of(userRequest).forEach(p -> {

				userUpdate.setDob(dob);
				userUpdate.setEmail(p.getEmail());
				userUpdate.setMobileNo(p.getMobileNo());
				userUpdate.setGender(p.getGender());
				userUpdate.setActive(true);
				userUpdate.setFirstName(p.getFirstName());
				userUpdate.setLastName(p.getLastName());
				userUpdate.setUpdatedBy(p.getUpdatedBy());
				userUpdate.setUserId(p.getUserId());
				userUpdate.setUpdatedOn(LocalDateTime.now());
			});
			userUpdate.setUserCredentials(new ArrayList<>(
					Arrays.asList(new UserCredentials(userUpdate.getUserCredentials().get(0).getUserCrednid(),
							userRequest.getUserId(), userUpdate.getUserCredentials().get(0).getHashPwdCode(),
							(userRequest.isAdmin() ? "ROLE_ADMIN" : userRequest.isUser() ? "ROLE_USER" : "ROLE_USER"),
							userUpdate))));

			User savedUser = userRepository.saveAndFlush(userUpdate);
			if (savedUser.getUpdatedOn().isAfter(beforeSave)) {
				return savedUser.getId();
			}
		} catch (Exception e) {
			throw new PracticeAppException(e.getMessage());
		}

		return 0l;
	}

	@Override
	public Long deleteUser(long id) {
		userRepository.deleteById(id);
		return id;
	}

}
