package com.demo.practice.service;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.demo.practice.entity.User;
import com.demo.practice.entity.UserCredentials;
import com.demo.practice.exception.PracticeAppException;
import com.demo.practice.model.DownloadImgResp;
import com.demo.practice.model.UserRequest;
import com.demo.practice.repository.UserCredentialsRepo;
import com.demo.practice.repository.UserRepo;
import com.demo.practice.util.CommonUtil;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

	@Value("${user.email.userid.exist}")
	String inValidMailMsg;

	@Value("${user.data.not.exist}")
	String dataNotFoundMsg;

	@Autowired
	UserRepo userRepository;

	@Autowired
	UserCredentialsRepo userCrednRepo;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@Value("${image.upload.allowed.extensions}")
	String allowedExtensions;

	@Autowired
	CommonUtil commonUtil;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<User> fetchUserById(String searchParamKey) {
		return userRepository.findByUserIdContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(
				searchParamKey, searchParamKey, searchParamKey, searchParamKey);
	}

	@Override
	public Long saveUser(UserRequest userRequest) {
		try {
			if (userRepository.existsByEmailAllIgnoreCase(userRequest.getEmail())) {
				logger.info("User with this email already exist. Please try with different email.");
				throw new PracticeAppException("User with this email already exist. Please try with different email.");
			}
			if (userRepository.existsByUserIdAllIgnoreCase(userRequest.getUserId())) {
				logger.info("User with this userId already exist. Please try with different userId.");
				throw new PracticeAppException(
						"User with this userId already exist. Please try with different userId.");
			}

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
					(userRequest.isAdmin() ? "ROLE_ADMIN" : userRequest.isUser() ? "ROLE_USER" : "ROLE_USER"), "", "",
					"", user)));

			if (userRepository.save(user) != null) {
				return user.getId();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new PracticeAppException(e.getMessage());
		}

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
							"", "", "", userUpdate))));

			User savedUser = userRepository.saveAndFlush(userUpdate);
			if (savedUser.getUpdatedOn().isAfter(beforeSave)) {
				return savedUser.getId();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new PracticeAppException(e.getMessage());
		}

		return 0l;
	}

	@Override
	public List<User> getUserEmailUsingUserId(String userId) {
		try {
			return userRepository.findEmailByUserId(userId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new PracticeAppException(e.getMessage());
		}

	}

	@Override
	public Map<String, String> uploadImage(MultipartFile file, long userCrednId) {
		Map<String, String> errorResponse = new HashMap<>();
		try {
			if (file.isEmpty()) {
				errorResponse.put("message", commonUtil.getValidationMessage("no.file.to.upload"));
				errorResponse.put("isSuccess", "false");
				return errorResponse;
			} else if (!allowedExtensions.contains(file.getContentType())) {
				errorResponse.put("message", commonUtil.getValidationMessage("invalid.file.type"));
				errorResponse.put("isSuccess", "false");
				return errorResponse;
			} else {
				// 3. Create upload directory if it does not exist
				String fileUploadPath = cacheManager.getCache("configCache").get("image.upload.path", String.class);
				File directory = new File(fileUploadPath);
				if (!directory.exists()) {
					directory.mkdirs();
				}

				// 4. Generate unique name to prevent naming collisions
				String originalFilename = file.getOriginalFilename();
				String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
				String randomName = UUID.randomUUID().toString().replace("-", "");
				String uniqueFilename = randomName + extension;

				String[] search = { "<serach_str>" };
				String[] replace = { String.valueOf(userCrednId) };

				UserCredentials userCreden = userCrednRepo.findById(userCrednId).orElseThrow(
						() -> new PracticeAppException(StringUtils.replaceEach(dataNotFoundMsg, search, replace)));

				userCreden.setImageUploadName(uniqueFilename);
				userCreden.setImageStorageIndicator("F");
				userCreden.setImageActualName(originalFilename);

				userCrednRepo.saveAndFlush(userCreden);

				String filePath = Paths.get(fileUploadPath, randomName).toString();
				file.transferTo(new File(filePath));

				errorResponse.put("message", commonUtil.getValidationMessage("image.upload.successfull"));
				errorResponse.put("isSuccess", "true");

				return errorResponse;
			}
		} catch (Exception e) {
			logger.error("Failed to upload image: " + e.getMessage());
			errorResponse.put("message", commonUtil.getValidationMessage("falied.to.upload"));
			errorResponse.put("isSuccess", "false");
			return errorResponse;
		}
	}

	@Override
	public DownloadImgResp downloadImage(long userCrednId) {
		try {
			String[] search = { "<serach_str>" };
			String[] replace = { String.valueOf(userCrednId) };

			UserCredentials userCreden = userCrednRepo.findById(userCrednId).orElseThrow(
					() -> new PracticeAppException(StringUtils.replaceEach(dataNotFoundMsg, search, replace)));

			String fileActualName = userCreden.getImageActualName();
			String fileUploadName = StringUtils.substringBeforeLast(userCreden.getImageUploadName(), ".");

			if (StringUtils.isNotEmpty(fileActualName) && StringUtils.isNotEmpty(fileUploadName)) {
				// 3. Create download directory if it does not exist
				String fileUploadPath = cacheManager.getCache("configCache").get("image.upload.path", String.class);
				File directory = new File(fileUploadPath + File.separator + fileUploadName);
				// file not exist check
				if (!directory.exists()) {
					return new DownloadImgResp(fileActualName, null, commonUtil.getValidationMessage("file.not.found"),
							false);
				}

				Resource resource = new FileSystemResource(directory);

				return new DownloadImgResp(fileActualName, resource,
						commonUtil.getValidationMessage("file.download.successfull"), true);
			} else {
				return new DownloadImgResp(fileActualName, null, commonUtil.getValidationMessage("file.not.found"),
						false);
			}

		} catch (Exception e) {
			logger.error("Failed to download image: " + e.getMessage());
		}
		return new DownloadImgResp();
	}
}
