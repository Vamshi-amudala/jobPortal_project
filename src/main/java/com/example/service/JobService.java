package com.example.service;

import com.example.dto.JobApplicationResponse;
//import com.example.dto.EmployerDto;
import com.example.dto.JobDto;
import com.example.entity.ApplicationStatus;
//import com.example.dto.JobSearchDto;
import com.example.entity.Job;
import com.example.entity.JobApplication;
import com.example.entity.JobStatus;
import com.example.entity.User;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
//import com.example.service.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JobApplicationRepository jobRepo;
	@Autowired
	private EmailService emailService;
	@Autowired
	private ResumeEmailService resumeEmailService;

	private User getCurrentUser() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByEmail(email).orElseThrow();
	}

	public Job postJob(JobDto dto) {
		User employer = getCurrentUser();
		Job job = new Job();
		job.setTitle(dto.getTitle());
		job.setExp(dto.getExp());
		job.setDescription(dto.getDescription());
		job.setLocation(dto.getLocation());
		job.setCompany(dto.getCompany());
		job.setSalary(dto.getSalary());
		job.setEmployer(employer);
		job.setStatus(dto.getStatus() != null ? dto.getStatus() : JobStatus.OPEN);

		return jobRepository.save(job);
	}

	public List<Job> postMultipleJobs(String email, List<Job> jobs) {
		User employer = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Employer not found"));

		for (Job job : jobs) {
			job.setEmployer(employer);

			if (job.getStatus() == null) {
				job.setStatus(JobStatus.OPEN);
			}
		}

		return jobRepository.saveAll(jobs);
	}

	public List<Job> getJobsByEmployer() {
		User employer = getCurrentUser();
		return jobRepository.findByEmployer(employer);
	}

	public Job updateJob(Long id, JobDto dto) {
		Job job = jobRepository.findById(id).orElseThrow();
		job.setTitle(dto.getTitle());
		job.setExp(dto.getExp());
		job.setDescription(dto.getDescription());
		job.setLocation(dto.getLocation());
		job.setCompany(dto.getCompany());
		job.setSalary(dto.getSalary());
		job.setStatus(dto.getStatus() != null ? dto.getStatus() : JobStatus.OPEN);
		return jobRepository.save(job);
	}

	public void deleteJob(Long id) {
		jobRepository.deleteById(id);
	}
	
	public Job updateJobStatus(Long id, JobStatus status) {
	    Job job = jobRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
	    job.setStatus(status);
	    return jobRepository.save(job);
	}


	public void updateStatus(Long applicationId, ApplicationStatus status) {
	    JobApplication app = jobRepo.findById(applicationId)
	        .orElseThrow(() -> new RuntimeException("Application not found"));

	    if (app.getStatus() == ApplicationStatus.WITHDRAWN || app.getStatus() == ApplicationStatus.REJECTED) {
	        throw new IllegalStateException("Cannot change status of withdrawn/rejected applications.");
	    }

	    app.setStatus(status);
	    jobRepo.save(app);

	    if (status == ApplicationStatus.SELECTED) {
	        String email = app.getApplicant().getEmail();
	        String title = app.getJob().getTitle();
	        String name = app.getApplicant().getFullName();

	        if (email != null && title != null && name != null) {
	            emailService.sendSelectedEmail(email, title, name);
	        } else {
	            throw new RuntimeException("Missing applicant email, job title, or candidate name");
	        }
	    }
	}


	public List<Job> getAllJobs() {
		return jobRepository.findByStatus(JobStatus.OPEN);
	}

	private JobDto toDto(Job job) {
//        EmployerDto employerDto = new EmployerDto(
//            job.getEmployer().getId(),
//            job.getEmployer().getEmail(),
//            job.getEmployer().getRole().name()
//        );

		JobDto dto = new JobDto();
		dto.setId(job.getId());
		dto.setTitle(job.getTitle());
		dto.setDescription(job.getDescription());
		dto.setLocation(job.getLocation());
		dto.setCompany(job.getCompany());
		dto.setSalary(job.getSalary());
		dto.setExp(job.getExp());
		dto.setStatus(job.getStatus());
//        dto.setEmployer(employerDto);

		return dto;
	}

	public List<JobDto> getJobsByEmployerDto() {
		User employer = getCurrentUser();
		List<Job> jobs = jobRepository.findByEmployer(employer);
		return jobs.stream().map(this::toDto).toList();
	}

	public List<JobDto> getAllJobsDto() {
		List<Job> jobs = jobRepository.findByStatus(JobStatus.OPEN);
		return jobs.stream().map(this::toDto).toList();
	}

	
	private JobApplicationResponse toJobApplicationResponse(JobApplication app) {
		JobApplicationResponse response = new JobApplicationResponse();
		response.setId(app.getId());
		response.setResumeUrl(app.getResumeUrl());
		response.setStatus(app.getStatus().name());

		JobApplicationResponse.JobInfo jobInfo = new JobApplicationResponse.JobInfo();
		jobInfo.setId(app.getJob().getId());
		jobInfo.setTitle(app.getJob().getTitle());
		jobInfo.setCompany(app.getJob().getCompany());
		jobInfo.setLocation(app.getJob().getLocation());
		jobInfo.setExp(app.getJob().getExp());
		response.setJob(jobInfo);

		JobApplicationResponse.ApplicantInfo applicantInfo = new JobApplicationResponse.ApplicantInfo();
		applicantInfo.setId(app.getApplicant().getId());
		applicantInfo.setFullName(app.getApplicant().getFullName());
		applicantInfo.setEmail(app.getApplicant().getEmail());
		applicantInfo.setPhone(app.getApplicant().getPhone());
		applicantInfo.setEducation(app.getApplicant().getEducation());
		response.setApplicant(applicantInfo);

		return response;
	}

	public List<JobApplicationResponse> getMyAppliedJobs() {
		User applicant = getCurrentUser();
		List<JobApplication> applications = jobRepo.findByApplicant(applicant);

		return applications.stream().map(this::toJobApplicationResponse).collect(Collectors.toList());
	}

	public JobApplicationResponse updateApplicationStatus(Long appId, ApplicationStatus status) {
	    JobApplication application = jobRepo.findById(appId)
	            .orElseThrow(() -> new RuntimeException("Application not found"));

	    User currentUser = getCurrentUser();
	    boolean isEmployer = application.getJob().getEmployer().getId().equals(currentUser.getId());
	    boolean isApplicant = application.getApplicant().getId().equals(currentUser.getId());

	    if (status == ApplicationStatus.WITHDRAWN && !isApplicant) {
	        throw new RuntimeException("Only the applicant can withdraw the application");
	    } else if (status != ApplicationStatus.WITHDRAWN && !isEmployer) {
	        throw new RuntimeException("Only the employer can update the application status");
	    }

	    
	    if (application.getStatus() == ApplicationStatus.WITHDRAWN || application.getStatus() == ApplicationStatus.REJECTED) {
	        throw new IllegalStateException("Cannot update application that is already " + application.getStatus());
	    }

	    application.setStatus(status);
	    JobApplication updated = jobRepo.save(application);

	    try {
	        
	        String toEmail = application.getApplicant().getEmail();
	        String applicantName = application.getApplicant().getFullName();
	        String jobTitle = application.getJob().getTitle();
	        String company = application.getJob().getCompany();

	        String subject = "";
	        String htmlContent = "";

	        switch (status) {
	            case SELECTED:
	                subject = "Offer Letter: Selected for " + jobTitle + " at " + company;
	                htmlContent = "<p>Dear " + applicantName + ",</p>"
	                        + "<p>Congratulations! We are thrilled to offer you the role of <strong>" + jobTitle
	                        + "</strong> at <strong>" + company + "</strong>.</p>"
	                        + "<p>Please find your official offer letter attached.</p>"
	                        + "<br><p>Best regards,<br><strong>Recruitment Team</strong><br>" + company + "</p>";
	                break;

	            case REJECTED:
	                subject = "Application Rejected for " + jobTitle;
	                htmlContent = "<p>Dear " + applicantName + ",</p>"
	                        + "<p>Thank you for applying for <strong>" + jobTitle + "</strong> at <strong>" + company
	                        + "</strong>. Unfortunately, we will not be proceeding with your application.</p>"
	                        + "<p>We wish you all the best in your job search.</p>"
	                        + "<br><p>Best regards,<br><strong>Recruitment Team</strong><br>" + company + "</p>";
	                break;

	            case WITHDRAWN:
	                subject = "Application Withdrawal Confirmation for " + jobTitle;
	                htmlContent = "<p>Dear " + applicantName + ",</p>"
	                        + "<p>Your application for <strong>" + jobTitle + "</strong> at <strong>" + company
	                        + "</strong> has been successfully withdrawn.</p>"
	                        + "<p>If this was unintentional, contact us at <a href='mailto:hr@" + company.toLowerCase().replaceAll(" ", "") + ".com'>hr@" + company.toLowerCase().replaceAll(" ", "") + ".com</a>.</p>"
	                        + "<br><p>Best regards,<br><strong>Recruitment Team</strong><br>" + company + "</p>";
	                break;

	            default:
	                subject = "Application Status Updated: " + status;
	                htmlContent = "<p>Dear " + applicantName + ",</p>"
	                        + "<p>Your application for <strong>" + jobTitle + "</strong> at <strong>" + company
	                        + "</strong> has been updated to: <strong>" + status.name() + "</strong>.</p>"
	                        + "<br><p>Best regards,<br>Recruitment Team</p>";
	        }

	        emailService.sendApplicationStatusEmail(toEmail, jobTitle, applicantName, status);


	    } catch (Exception e) {
	        System.err.println("Failed to send status update email: " + e.getMessage());
	    }

	    return toJobApplicationResponse(updated);
	}


	public List<JobApplicationResponse> getApplicationsForMyJobs() {
		User employer = getCurrentUser();
		List<JobApplication> applications = jobRepo.findByEmployer(employer);

		return applications.stream().map(this::toJobApplicationResponse).collect(Collectors.toList());
	}

	public JobApplication findApplicationById(Long id) {
		return jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
	}

	public List<JobDto> getJobsByLocation(String location) {
		List<Job> jobs = jobRepository.findByLocation(location);
		return jobs.stream().map(this::toDto).toList();
	}

	public List<JobDto> getJobsByTitle(String title) {
		List<Job> jobs = jobRepository.findByTitle(title);
		return jobs.stream().map(this::toDto).toList();

	}

	public List<JobDto> getJobsByCompany(String company) {
		List<Job> jobs = jobRepository.findByCompany(company);
		return jobs.stream().map(this::toDto).toList();

	}

	public List<JobDto> getJobsSorted(String sortBy, String order) {
		List<Job> jobs;

		String sortField = (sortBy != null) ? sortBy.toLowerCase() : "";
		String sortOrder = (order != null) ? order.toLowerCase() : "asc";

		switch (sortField) {
		case "salary":
			jobs = sortOrder.equals("desc") ? jobRepository.findAllOrderBySalaryDesc()
					: jobRepository.findAllOrderBySalaryAsc();
			break;
		case "exp":
			jobs = sortOrder.equals("desc") ? jobRepository.findAllOrderByExperienceDesc()
					: jobRepository.findAllOrderByExperienceAsc();
			break;
		default:

			if ("desc".equals(sortOrder)) {
				jobs = jobRepository.findAllOrderByExperienceDesc();
			} else if ("asc".equals(sortOrder)) {
				jobs = jobRepository.findAllOrderByExperienceAsc();
			} else {

				jobs = jobRepository.findByStatus(JobStatus.OPEN);
			}
		}

		return jobs.stream().map(this::toDto).collect(Collectors.toList());
	}

}
