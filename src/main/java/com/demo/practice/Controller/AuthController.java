package com.demo.practice.Controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.practice.model.AuthRequest;
import com.demo.practice.model.AuthResponse;
import com.demo.practice.model.Response;
import com.demo.practice.model.UserRequest;
import com.demo.practice.model.UserRequest.OnCreate;
import com.demo.practice.service.JWTService;
import com.demo.practice.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;
	private final UserService userService;

	public AuthController(AuthenticationManager authenticationManager, JWTService jwtService, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userService = userService;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AuthRequest request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtService.generateToken(userDetails);

		return new AuthResponse(token, "Bearer", jwtService.getExpirationTime(), "Login successful",
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
			return ResponseEntity.badRequest()
					.body(new Response(Response.increment(), errorMessage.toString(), false, null));
		}
		Long insertedVal = userService.saveUser(userRequest);
		return ResponseEntity.ok(insertedVal != 0
				? new Response(insertedVal,
						"User registration done succesfully, you can login from <a href='/login'>here</a>", true, null)
				: new Response(Response.increment(), "Data updation failed", false, null));

	}
}
