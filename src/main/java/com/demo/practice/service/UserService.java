package com.demo.practice.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.demo.practice.entity.User;
import com.demo.practice.model.DownloadImgResp;
import com.demo.practice.model.UserRequest;

public interface UserService {

	List<User> fetchUserById(String searchParamKey);

	Long saveUser(UserRequest userRequest);

	long updateUser(UserRequest userRequest, long id);

	List<User> getUserEmailUsingUserId(String userId);

	Map<String, String> uploadImage(MultipartFile file, long userCrednId);

	DownloadImgResp downloadImage(long userCrednId);

}
