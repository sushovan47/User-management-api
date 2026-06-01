package com.demo.practice.Controller;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.practice.model.Response;
import com.demo.practice.model.UserRequest;
import com.demo.practice.model.UserRequest.OnUpdate;
import com.demo.practice.service.UserService;
import com.demo.practice.util.CommonUtil;

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/user")
@NoArgsConstructor
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@GetMapping(value = "/fetchUserById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> fetchUserById(@RequestParam(required = true) String searchParamKey) {
		return Optional.ofNullable(userService.fetchUserById(searchParamKey)).filter(l -> !l.isEmpty())
				.map(users -> ResponseEntity.ok(new Response(Response.increment(),
						commonUtil.getValidationMessage("user.data.found"), true, Optional.of(users),
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(Response.increment(), commonUtil.getValidationMessage("user.data.no.found"),
								false, Optional.empty(), StringUtils.defaultString(
										cacheManager.getCache("configCache").get("app-name", String.class)))));

	}

	@PutMapping(value = "/updateUser/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> updateUser(@Valid @Validated(OnUpdate.class) @RequestBody UserRequest userRequest,
			BindingResult bindingResult, @PathVariable(required = true) long id) {
		if (bindingResult.hasErrors()) {
			StringBuilder errorMessage = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				errorMessage.append(error.getDefaultMessage()).append("; ");
			});
			return ResponseEntity.badRequest().body(new Response(Response.increment(), errorMessage.toString(), false,
					null,
					StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));
		}

		Long updatedVal = userService.updateUser(userRequest, id);
		return ResponseEntity.ok(updatedVal != 0
				? new Response(updatedVal, commonUtil.getValidationMessage("user.data.update.success"), true, null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))
				: new Response(0l, commonUtil.getValidationMessage("user.no.db.changes"), false, null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));
	}
}
