package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String company;

    private String salary;    

    private String exp;      

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;
}
