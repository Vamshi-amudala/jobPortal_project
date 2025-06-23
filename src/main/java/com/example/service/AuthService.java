package com.example.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.dto.AuthResponseDto;
import com.example.dto.LoginDto;
import com.example.dto.UserRegistrationDto;
import com.example.entity.User;
import com.example.exception.EmailAlreadyExistsException;
import com.example.repository.UserRepository;

@Service
public class AuthService {

	  @Autowired
	    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    public String registerUser(UserRegistrationDto dto) {
    	
    	String cleanEmail = dto.getEmail().trim().toLowerCase();
    	try {
    	    if (userRepository.existsByEmail(cleanEmail)) {
    	        throw new EmailAlreadyExistsException("Email already registered, please use different email..!");
    	    }

    	    User user = new User();
    	    user.setFullName(dto.getFullName());
    	    user.setEmail(cleanEmail);
    	    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    	    user.setRole(dto.getRole());

    	    userRepository.save(user);
    	    return "User registered successfully as " + dto.getRole();
    	} catch (Exception e) {
    	    e.printStackTrace(); // to see the real reason
    	    throw new RuntimeException("Registration failed: " + e.getMessage());
    	}

    }


  

    public AuthResponseDto login(LoginDto loginDto) {
        System.out.println("Email: " + loginDto.getEmail());
        System.out.println("Password (raw): " + loginDto.getPassword());

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getEmail(), loginDto.getPassword()
                )
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            AuthResponseDto response = new AuthResponseDto();
            response.setMessage("Login successful");
            return response;
        } catch (Exception e) {
            e.printStackTrace(); 
            throw new RuntimeException("Login failed");
        }
    }



    
    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
        user.setOtpCode(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public void verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        
        if (user.getOtpCode() == null || !otp.equals(user.getOtpCode())) {
            throw new BadCredentialsException("Invalid OTP");
        }

        if (user.getOtpExpiration() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("OTP has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        userRepository.save(user);

        // Send confirmation email
        emailService.sendResetPasswordConfirmationEmail(user.getEmail());
    }

    
    

}
