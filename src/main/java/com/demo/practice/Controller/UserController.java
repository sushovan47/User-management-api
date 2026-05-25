package com.demo.practice.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/user")
@NoArgsConstructor
public class UserController {

	@Autowired
	UserService userService;

	@GetMapping(value = "/fetchAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> fetchAllUsers() {
		return Optional.ofNullable(userService.fetchAllUserList()).filter(list -> !list.isEmpty())
				.map(users -> ResponseEntity
						.ok(new Response(Response.increment(), "Data found successfully", true, Optional.of(users))))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(Response.increment(), "No data found", false, Optional.empty())));

	}

	@GetMapping(value = "/fetchUserById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> fetchUserById(@RequestParam(required = true) String searchParamKey) {
		return Optional.ofNullable(userService.fetchUserById(searchParamKey)).filter(l -> !l.isEmpty())
				.map(users -> ResponseEntity
						.ok(new Response(Response.increment(), "Data found successfully", true, Optional.of(users))))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(Response.increment(), "No data found", false, Optional.empty())));

	}

	@PutMapping(value = "/updateUser/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> updateUser(@Valid @Validated(OnUpdate.class) @RequestBody UserRequest userRequest,
			BindingResult bindingResult, @PathVariable(required = true) long id) {
		if (bindingResult.hasErrors()) {
			StringBuilder errorMessage = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				errorMessage.append(error.getDefaultMessage()).append("; ");
			});
			return ResponseEntity.badRequest()
					.body(new Response(Response.increment(), errorMessage.toString(), false, null));
		}

		Long updatedVal = userService.updateUser(userRequest, id);
		return ResponseEntity.ok(updatedVal != 0 ? new Response(updatedVal, "Data updated succesfully", true, null)
				: new Response(0l, "No changes detected; no DB update performed", false, null));
	}

	@DeleteMapping(value = "/deleteUser/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteUser(@PathVariable(required = true) long id) {

		Long deletedVal = userService.deleteUser(id);
		return ResponseEntity.ok(deletedVal != 0 ? new Response(deletedVal, "Data deleted succesfully", true, null)
				: new Response(0l, "Data updation failed", false, null));
	}

}
