package com.example.controller;

import com.example.dto.UserProfileDto;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;

import java.util.Map;

//import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
//import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepo;

   
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized: No authentication found");
        }

        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userService.getProfileDto(email));

    }

    
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfileDto dto) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(userService.updateProfileDto(email, dto));
    }
   }
