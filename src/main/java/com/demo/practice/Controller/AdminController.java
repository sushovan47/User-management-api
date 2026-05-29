package com.demo.practice.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@NoArgsConstructor
public class AdminController {

	@Autowired
	AdminService adminService;

	@GetMapping(value = "/fetchAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> fetchAllUsers() {
		return Optional.ofNullable(adminService.fetchAllUserList()).filter(list -> !list.isEmpty())
				.map(users -> ResponseEntity
						.ok(new Response(Response.increment(), "Data found successfully", true, Optional.of(users))))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(Response.increment(), "No data found", false, Optional.empty())));

	}

	@DeleteMapping(value = "/deleteUser/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteUser(@PathVariable(required = true) long id) {

		Long deletedVal = adminService.deleteUser(id);
		return ResponseEntity.ok(deletedVal != 0 ? new Response(deletedVal, "Data deleted succesfully", true, null)
				: new Response(0l, "Data updation failed", false, null));
	}

}
