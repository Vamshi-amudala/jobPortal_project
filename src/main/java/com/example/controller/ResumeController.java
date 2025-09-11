package com.example.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            
            String resumeUrl = "/uploads/resumes/" + filename;
            user.setResumeUrl(resumeUrl);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("resumeUrl", resumeUrl));
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
            return ResponseEntity.ok(Map.of("resumeUrl", user.getResumeUrl()));
        } else {
            return ResponseEntity.ok(Map.of("resumeUrl", (String) null));
        }
    }
    
    // 🆕 NEW ENDPOINT: View/Download resume file
    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewResume(@PathVariable String filename, Authentication auth) {
        try {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Security check: ensure the user owns this file
            if (user.getResumeUrl() == null || !user.getResumeUrl().contains(filename)) {
                return ResponseEntity.notFound().build();
            }
            
            // Resolve file path
            Path filePath = Paths.get(resumeDir).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = determineContentType(filename);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.EXPIRES, "0")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("Error serving resume file: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String lowerCase = filename.toLowerCase();
        if (lowerCase.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerCase.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerCase.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else {
            return "application/octet-stream";
        }
    }
    
    @GetMapping("/view/application/{filename}")
    public ResponseEntity<Resource> viewApplicationResume(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("").toAbsolutePath().resolve("uploads/resumes").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType;
            if (filename.toLowerCase().endsWith(".pdf")) contentType = "application/pdf";
            else if (filename.toLowerCase().endsWith(".doc")) contentType = "application/msword";
            else if (filename.toLowerCase().endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            else contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}