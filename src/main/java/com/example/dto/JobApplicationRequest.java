package com.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class JobApplicationRequest {
    private String applicantName;
    private String email;
    private String education;
    private String jobTitle;
    private String resumeUrl;
    private String experience;
}
