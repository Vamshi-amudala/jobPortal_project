package com.example.dto;

import lombok.Data;

@Data
public class JobApplicationResponse {
    private Long id;
    private String resumeUrl;
    private String status;
    private JobInfo job;
    private ApplicantInfo applicant;

    @Data
    public static class JobInfo {
        private Long id;
        private String title;
        private String company;
        private String location;
        private String exp;
    }

    @Data
    public static class ApplicantInfo {
        private Long id;
        private String fullName;
        private String email;
        private String education;
        private String phone;
    }
}
