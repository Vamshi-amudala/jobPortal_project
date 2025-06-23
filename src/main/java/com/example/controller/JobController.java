package com.example.controller;

import com.example.dto.JobDto;
import com.example.entity.Job;
import com.example.entity.JobStatus;
import com.example.service.JobService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/multiple")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<Job>> addMultipleJobs(
        @RequestParam String email,
        @RequestBody List<Job> jobs) {
        List<Job> savedJobs = jobService.postMultipleJobs(email, jobs);
        return ResponseEntity.ok(savedJobs);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDto> postJob(@Valid @RequestBody JobDto dto) {
        Job job = jobService.postJob(dto);
        return ResponseEntity.ok(toDto(job));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobDto>> getMyJobs() {
        var jobs = jobService.getJobsByEmployer();
        var jobDtos = jobs.stream()
                          .map(this::toDto)
                          .collect(Collectors.toList());
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/location")
    public ResponseEntity<List<JobDto>> getJobsByLocation(@RequestParam String location) {
        return ResponseEntity.ok(jobService.getJobsByLocation(location));
    }

    @GetMapping("/title")
    public ResponseEntity<List<JobDto>> getJobsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(jobService.getJobsByTitle(title));
    }

    @GetMapping("/company")
    public ResponseEntity<List<JobDto>> getJobsByCompany(@RequestParam String company) {
        return ResponseEntity.ok(jobService.getJobsByCompany(company));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDto> updateJob(@PathVariable Long id, @RequestBody JobDto dto) {
        Job updatedJob = jobService.updateJob(id, dto);
        return ResponseEntity.ok(toDto(updatedJob));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }


    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDto> updateJobStatus(@PathVariable Long id, @RequestParam JobStatus status) {
        Job updatedJob = jobService.updateJobStatus(id, status);
        return ResponseEntity.ok(toDto(updatedJob));
    }

    @GetMapping
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('EMPLOYER')")
    public ResponseEntity<List<JobDto>> getAllJobs() {
        var jobs = jobService.getAllJobs();
        var jobDtos = jobs.stream()
                          .map(this::toDto)
                          .collect(Collectors.toList());
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<JobDto>> getJobsSorted(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order) {
        List<JobDto> jobs = jobService.getJobsSorted(sortBy, order);
        return ResponseEntity.ok(jobs);
    }

    private JobDto toDto(Job job) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setCompany(job.getCompany());
        dto.setSalary(job.getSalary());
        dto.setExp(job.getExp());
        dto.setStatus(job.getStatus());
        return dto;
    }
    
    
}
