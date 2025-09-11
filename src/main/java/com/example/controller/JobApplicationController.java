package com.example.controller;

import com.example.dto.JobApplicationRequest;
import com.example.dto.JobApplicationResponse;
import com.example.entity.ApplicationStatus;
import com.example.entity.*;
import com.example.entity.JobApplication;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobRepository;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final JobService jobService;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;

    @PostMapping("/apply")
    public ResponseEntity<JobApplicationResponse> applyForJob(@RequestBody @Valid JobApplicationRequest request) {
        JobApplication application = jobApplicationService.applyForJob(request);
        return ResponseEntity.ok(jobApplicationService.toResponse(application));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(jobService.getMyAppliedJobs());
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public List<Job> getAvailableJobs(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> appliedJobIds = jobApplicationRepository
                .findByApplicant_Id(user.getId()).stream()
                .filter(app -> List.of(
                        ApplicationStatus.APPLIED.name(),
                        ApplicationStatus.UNDER_REVIEW.name(),
                        ApplicationStatus.INTERVIEWING.name(),
                        ApplicationStatus.SELECTED.name()
                ).contains(app.getStatus().name()))
                .map(app -> app.getJob().getId())
                .toList();

        return jobRepository.findAll().stream()
                .filter(job -> !appliedJobIds.contains(job.getId()))
                .toList();
    }
    
    @GetMapping("/employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForMyJobs() {
        return ResponseEntity.ok(jobService.getApplicationsForMyJobs());
    }

    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        ApplicationStatus status = ApplicationStatus.valueOf(payload.get("status"));
        return ResponseEntity.ok(jobService.updateApplicationStatus(id, status));
    }
    
    @PutMapping("/applied-jobs/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> withdrawApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        return ResponseEntity.ok(jobApplicationService.withdrawApplication(id, email));
    }
}
