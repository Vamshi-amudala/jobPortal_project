package com.example.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
@JsonInclude(JsonInclude.Include.NON_NULL) 
@Data
public class UserProfileDto {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String education;
    private String exp;
    private List<String> skills;  
    private List<ProjectDto> projects;
    
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String designation;

}
