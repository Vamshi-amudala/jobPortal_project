package com.example.dto;

import com.example.entity.JobStatus;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class JobDto {
    private Long id;  
    private String title;
    
    @Column(length = 2000)
    private String description;
    private String location;
    private String company;
    private String salary;
    private String exp;
//    private EmployerDto employer;  
    private JobStatus status;
}
