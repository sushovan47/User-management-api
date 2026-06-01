package com.demo.practice.Controller;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.practice.model.AuthRequest;
import com.demo.practice.model.AuthResponse;
import com.demo.practice.model.OtpRequest;
import com.demo.practice.model.OtpVerifyRequest;
import com.demo.practice.model.ResetPasswordRequest;
import com.demo.practice.model.Response;
import com.demo.practice.model.UserRequest;
import com.demo.practice.model.UserRequest.OnCreate;
import com.demo.practice.service.JWTService;
import com.demo.practice.service.OtpService;
import com.demo.practice.service.UserService;
import com.demo.practice.util.CommonUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class CommonController {

	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;
	private final UserService userService;
	private final OtpService otpService;
	private final CommonUtil commonUtils;
	private final CacheManager cacheManager;

	public CommonController(AuthenticationManager authenticationManager, JWTService jwtService, UserService userService,
			OtpService otpService, CommonUtil commonUtils, @Qualifier("localCacheManager") CacheManager cacheManager) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userService = userService;
		this.otpService = otpService;
		this.commonUtils = commonUtils;
		this.cacheManager = cacheManager;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AuthRequest request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtService.generateToken(userDetails);

		return new AuthResponse(token, "Bearer", jwtService.getExpirationTime(),
				commonUtils.getValidationMessage("user.login.success"),
				StringUtils.replaceEach(userDetails.getAuthorities().toString(), new String[] { "[", "]" },
						new String[] { "", "" }),
				userDetails.getUsername(), true);
	}

	@PostMapping(value = "/saveUser", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> saveUser(@Valid @Validated(OnCreate.class) @RequestBody UserRequest userRequest,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			StringBuilder errorMessage = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				errorMessage.append(error.getDefaultMessage()).append("; ");
			});
			return ResponseEntity.badRequest().body(new Response(Response.increment(), errorMessage.toString(), false,
					null,
					StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));
		}
		Long insertedVal = userService.saveUser(userRequest);
		return ResponseEntity.ok(insertedVal != 0
				? new Response(insertedVal, commonUtils.getValidationMessage("user.regn.success"), true, null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))
				: new Response(Response.increment(), commonUtils.getValidationMessage("user.data.update.failed"), false,
						null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));

	}

	@GetMapping(value = "/getUserEmailByUserId", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getUserEmailUsingUserId(@RequestParam(required = true) String userId) {
		return Optional.ofNullable(userService.getUserEmailUsingUserId(userId))
				.map(email -> ResponseEntity.ok(new Response(Response.increment(),
						!email.isEmpty() ? commonUtils.getValidationMessage("user.data.found")
								: commonUtils.getValidationMessage("user.no.mail.found") + userId + "</b>",
						!email.isEmpty() ? true : false, Optional.of(email),
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))))
				.orElse(ResponseEntity.status(404)
						.body(new Response(Response.increment(), commonUtils.getValidationMessage("user.data.no.found"),
								false, Optional.empty(), StringUtils.defaultString(
										cacheManager.getCache("configCache").get("app-name", String.class)))));

	}

	@PostMapping(value = "/generateOtpNSendMail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> generateOtpAndSendMail(@RequestBody OtpRequest otpRequest) {

		otpService.generateAndSendOtp(otpRequest.getUserId(), otpRequest.getEmail());
		return ResponseEntity.ok(new Response(1,
				commonUtils.getValidationMessage("user.otp.sent") + otpRequest.getEmail() + "</b>", true, null,
				StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));

	}

	@PostMapping(value = "/verifyOtp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> generateOtpAndSendMail(@RequestBody OtpVerifyRequest otpVerifyRequest) {

		boolean isVerified = otpService.verifyOtp(otpVerifyRequest.getUserId(), otpVerifyRequest.getOtp(),
				otpVerifyRequest.getEmail(), otpVerifyRequest.getUserPkId());

		return ResponseEntity.ok(new Response(1,
				isVerified ? commonUtils.getValidationMessage("user.otp.verified")
						: commonUtils.getValidationMessage("user.otp.incorrect"),
				isVerified, null,
				StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));

	}

	@PostMapping(value = "/resetPassword", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> resetPassword(@RequestBody ResetPasswordRequest resetPwdReq) {

		boolean isReset = otpService.resetPassword(resetPwdReq.getUserPkId(), resetPwdReq.getToken(),
				resetPwdReq.getHashCode());

		return ResponseEntity.ok(new Response(1,
				isReset ? commonUtils.getValidationMessage("user.reset.password")
						: commonUtils.getValidationMessage("user.reset.faliure"),
				isReset, null,
				StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));

	}

	@PostMapping(value = "/validLink", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> validationCheckOnLink(@RequestBody ResetPasswordRequest resetPwdReq) {

		boolean isValid = otpService.validLink(resetPwdReq.getUserPkId(), resetPwdReq.getToken());

		return ResponseEntity.ok(new Response(1,
				isValid ? commonUtils.getValidationMessage("user.link.valid")
						: commonUtils.getValidationMessage("user.link.invalid"),
				isValid, null,
				StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));

	}
}
