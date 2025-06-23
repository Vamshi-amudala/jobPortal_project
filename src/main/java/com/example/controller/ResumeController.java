package com.example.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.User;
import com.example.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final UserRepository userRepository;

    @Value("${app.resume.upload.dir:uploads/resumes/}")
    private String resumeDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, Authentication auth) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Resume file is missing");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dirPath = Paths.get(resumeDir);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setResumeUrl("/uploads/resumes/" + filename); // optional: store resume URL
            userRepository.save(user);

            return ResponseEntity.ok("✅ Resume uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("❌ Failed to upload resume");
        }
    }

    @GetMapping
    public ResponseEntity<?> getResumeLink(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResumeUrl() != null) {
            return ResponseEntity.ok(user.getResumeUrl());
        } else {
            return ResponseEntity.ok("No resume uploaded yet.");
        }
    }
}
