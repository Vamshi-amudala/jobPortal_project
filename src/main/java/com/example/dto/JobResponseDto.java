package com.example.dto;

import com.example.entity.JobStatus;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDto {
    private Long id;
    private String title;
    @Column(length = 2000)
    private String description;
    private String location;
    private String company;
    private String salary;
    private String exp;
    private JobStatus status;
}
