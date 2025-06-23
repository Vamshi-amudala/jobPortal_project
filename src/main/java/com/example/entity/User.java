package com.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

 
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "education")
    private String education;
    
    private String exp;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills;
    
    private String resetToken;
    
    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_projects", joinColumns = @JoinColumn(name = "user_id"))
    private List<Project> projects = new ArrayList<>();
    
    
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_description", length = 1000)
    private String companyDescription;

    @Column(name = "designation") 
    private String designation;

    private String resumeUrl;

}
