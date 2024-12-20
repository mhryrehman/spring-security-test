package com.example.spring_security_test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("test")
public class SmartConfigurationController {

	@GetMapping("/smart-configuration")
	public ResponseEntity<Map<String, Object>> getSmartConfiguration() {
		Map<String, Object> response = Map.of(
			"authorization_endpoint", "response"
		);
		String token = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if(null == token){
			throw new NullPointerException("this is exception");
		}

		return ResponseEntity.ok()
			.headers(headers)
			.body(response);
	}

	@GetMapping("/id")
	public ResponseEntity<Map<String, Object>> getById() {
		Map<String, Object> response = Map.of(
				"authorization_endpoint", "response"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.headers(headers)
				.body(response);
	}
}


