package com.demo.practice.Controller;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.practice.model.Response;
import com.demo.practice.service.AdminService;
import com.demo.practice.util.CommonUtil;

import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@NoArgsConstructor
public class AdminController {

	@Autowired
	AdminService adminService;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	@Qualifier("localCacheManager")
	CacheManager cacheManager;

	@GetMapping(value = "/fetchAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> fetchAllUsers() {
		return Optional.ofNullable(adminService.fetchAllUserList()).filter(list -> !list.isEmpty())
				.map(users -> ResponseEntity.ok(new Response(Response.increment(),
						commonUtil.getValidationMessage("user.data.found"), true, Optional.of(users),
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(Response.increment(), commonUtil.getValidationMessage("user.data.no.found"),
								false, Optional.empty(), StringUtils.defaultString(
										cacheManager.getCache("configCache").get("app-name", String.class)))));

	}

	@DeleteMapping(value = "/deleteUser/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteUser(@PathVariable(required = true) long id) {

		Long deletedVal = adminService.deleteUser(id);
		return ResponseEntity.ok(deletedVal != 0
				? new Response(deletedVal, commonUtil.getValidationMessage("user.data.delete"), true, null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class)))
				: new Response(0l, commonUtil.getValidationMessage("user.data.update.failed"), false, null,
						StringUtils.defaultString(cacheManager.getCache("configCache").get("app-name", String.class))));
	}
}
