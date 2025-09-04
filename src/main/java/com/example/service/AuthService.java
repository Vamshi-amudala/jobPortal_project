package com.example.service;

import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.Random;
//import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.dto.AuthResponseDto;
import com.example.dto.LoginDto;
import com.example.dto.RegistrationResponseDto;
import com.example.dto.UserRegistrationDto;
import com.example.entity.User;
import com.example.exception.EmailAlreadyExistsException;
import com.example.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

    public RegistrationResponseDto registerUser(UserRegistrationDto dto) {
        String cleanEmail = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new EmailAlreadyExistsException(
                "Email already registered, please use a different email!"
            );
        }

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        userRepository.save(user);

        return new RegistrationResponseDto(
            "User registered successfully as " + dto.getRole().name(),
            user.getEmail(),
            user.getRole().name()
        );
    }


  
    public AuthResponseDto login(LoginDto loginDto, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()
            )
        );

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .map(r -> r.replace("ROLE_", ""))
            .orElse("USER");

        User user = userRepository.findByEmail(loginDto.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponseDto(
            "Login successful",
            user.getEmail(),
            role,
            user.getFullName()
        );
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


    
    public void verifyOtpAndResetPassword(String email, String otp, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        
        if (user.getOtpCode() == null || !otp.equals(user.getOtpCode())) {
            throw new BadCredentialsException("Invalid OTP");
        }

        if (user.getOtpExpiration() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("OTP has expired");
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Set the password once
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear OTP fields
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        userRepository.save(user);

        // Send confirmation email
        emailService.sendResetPasswordConfirmationEmail(user.getEmail());
    }


    
    

}
