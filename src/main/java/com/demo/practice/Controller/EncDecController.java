package com.demo.practice.Controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/encDec")
public class EncDecController {

	@Autowired
	@Qualifier("jasyptStringEncryptor")
	StringEncryptor jasyptStringEncryptor;

	@PostMapping(value = "/encryptText", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> encryptedTextVal(@RequestBody Map<String, String> request) {

		if (StringUtils.isNotEmpty(request.get("userName"))) {
			return ResponseEntity
					.ok(Map.of("encryptedText", jasyptStringEncryptor.encrypt(request.get("decryptedText"))));
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@PostMapping(value = "/decryptText", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> decryptedTextVal(@RequestBody Map<String, String> request) {

		if (StringUtils.isNotEmpty(request.get("userName"))) {
			return ResponseEntity
					.ok(Map.of("decryptedText", jasyptStringEncryptor.decrypt(request.get("encryptedText"))));
		} else {
			return ResponseEntity.notFound().build();
		}

	}

}
