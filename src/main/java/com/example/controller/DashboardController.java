package com.example.controller;

import java.util.*;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

	@GetMapping("/dashboard-data")
	public ResponseEntity<?> getDashboardData(Authentication auth) {
	    if (auth == null || !auth.isAuthenticated()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
	    }

	    String email = auth.getName();
	    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
	    List<String> roles = authorities.stream()
	            .map(GrantedAuthority::getAuthority)
	            .toList();

	    Map<String, Object> data = new HashMap<>();
	    data.put("email", email);
	    data.put("role", roles); // or roles.get(0) if only one role

	    return ResponseEntity.ok(data);
	}
	
	

}