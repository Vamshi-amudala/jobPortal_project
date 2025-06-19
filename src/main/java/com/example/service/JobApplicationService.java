package com.example.service;

import com.example.dto.JobApplicationRequest;
import com.example.dto.JobApplicationResponse;
import com.example.entity.*;
import com.example.exception.*;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

//    @Value("${app.upload.dir:uploads/resumes/}")
//    private String uploadDir;
	@Autowired
	private EmailService emailService;


    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmailService email;

    public JobApplication applyForJob(JobApplicationRequest request) {
    	 String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User applicant = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Job> jobs = jobRepository.findByTitle(request.getJobTitle());
        if (jobs.isEmpty()) {
            throw new RuntimeException("Job not found");
        }

        Job job = jobs.get(0);

        Optional<JobApplication> existingOpt = applicationRepository.findByApplicantAndJob(applicant, job);

        if (existingOpt.isPresent()) {
            JobApplication existing = existingOpt.get();

            if (existing.getStatus() == ApplicationStatus.APPLIED || existing.getStatus() == ApplicationStatus.SELECTED) {
                throw new JobApplicationExistsException("You have already applied or have been selected for this job.");
            }

            applicationRepository.delete(existing);
        }

        JobApplication application = new JobApplication();
        application.setApplicant(applicant);
        application.setJob(job);
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(ApplicationStatus.APPLIED);

        JobApplication savedApp = applicationRepository.save(application);

        sendConfirmationEmail(applicant, job);

        return savedApp;
    }
    


//    public JobApplicationResponse applyForJobWithFile(JobApplicationRequest request, MultipartFile resumeFile) {
//        String filename = saveResumeFile(resumeFile);
//        request.setResumeUrl("/uploads/resumes/" + filename);
//        return applyForJob(request);
//    }
    
//    public JobApplicationResponse applyForJobWithFile(JobApplicationRequest request, MultipartFile resumeFile) {
//        String filename = saveResumeFile(resumeFile);
//        request.setResumeUrl("/uploads/resumes/" + filename);
//        JobApplication savedApp = applyForJob(request);
//        return toResponse(savedApp);
//    }


//    private String saveResumeFile(MultipartFile file) {
//        try {
//            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path uploadPath = Paths.get(uploadDir);
//
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
//            return filename;
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to save resume file", e);
//        }
//    }

    private void sendConfirmationEmail(User applicant, Job job) {
        String subject = "Job Application Submitted";
        String htmlContent = "<p>Dear " + applicant.getFullName() + ",</p>"
                + "<p>Thank you for your interest in the position of <strong>" + job.getTitle() + "</strong> at <strong>" + job.getCompany() + "</strong>.</p>"
                + "<p>We have received your application and our recruitment team is now reviewing your profile carefully. "
                + "You can expect to hear from us within the next few days regarding the status of your application.</p>"
                + "<p>In the meantime, we encourage you to keep an eye on your inbox for any updates or requests for additional information.</p>"
                + "<p>Should you have any questions or need further assistance, please do not hesitate to reach out to our support team at <a href='mailto:jobloomhq@gmail.com'>jobloomhq@gmail.com</a>.</p>"
                + "<p>Best of luck! We appreciate your effort in taking the next step towards your career aspirations.</p>"
                + "<p><i>Sincerely,</i><br/>"
                + "<i>The Job Portal Recruitment Team</i></p>"
                + "<hr/>"
                + "<p><small>Please note: This is an automated message. Do not reply directly to this email.</small></p>";

        try {
            File attachment = generateJobApplicationFile(applicant, job);
            email.sendApplicationConfirmationEmail(
                applicant.getEmail(),
                subject,
                htmlContent,
                attachment
            );
        } catch (Exception e) {
            System.out.println("❌ Email sending failed: " + e.getMessage());
        }
    }

    private File generateJobApplicationFile(User user, Job job) throws IOException {
        File file = new File("job_application_details_" + user.getId() + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Job Application Details\n");
            writer.write("=======================\n");
            writer.write("Applicant Name: " + user.getFullName() + "\n");
            writer.write("Email: " + user.getEmail() + "\n");
            writer.write("Phone: " + user.getPhone() + "\n");
            writer.write("Education: " + user.getEducation() + "\n\n");

            writer.write("Job Title: " + job.getTitle() + "\n");
            writer.write("Company: " + job.getCompany() + "\n");
            writer.write("Location: " + job.getLocation() + "\n");
            writer.write("Experience Required: " + job.getExp() + "\n");
        }
        return file;
    }

    public JobApplicationResponse toResponse(JobApplication app) {
        JobApplicationResponse resp = new JobApplicationResponse();
        resp.setId(app.getId());
        resp.setResumeUrl(app.getResumeUrl());
        resp.setStatus(app.getStatus().name());

        JobApplicationResponse.JobInfo jobInfo = new JobApplicationResponse.JobInfo();
        jobInfo.setId(app.getJob().getId());
        jobInfo.setTitle(app.getJob().getTitle());
        jobInfo.setCompany(app.getJob().getCompany());
        jobInfo.setLocation(app.getJob().getLocation());
        jobInfo.setExp(app.getJob().getExp());
        resp.setJob(jobInfo);

        JobApplicationResponse.ApplicantInfo applicantInfo = new JobApplicationResponse.ApplicantInfo();
        applicantInfo.setId(app.getApplicant().getId());
        applicantInfo.setFullName(app.getApplicant().getFullName());
        applicantInfo.setEmail(app.getApplicant().getEmail());
        applicantInfo.setEducation(app.getApplicant().getEducation());
        applicantInfo.setPhone(app.getApplicant().getPhone());
        resp.setApplicant(applicantInfo);

        return resp;
    }
    
    
    public void updateJobStatus(Long id, ApplicationStatus status) {
        JobApplication app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() == ApplicationStatus.WITHDRAWN || app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot update application that is already " + app.getStatus());
        }

        app.setStatus(status);
        applicationRepository.save(app);

        // Optional: Email notification
        if (status == ApplicationStatus.SELECTED) {
            email.sendSelectedEmail(app.getApplicant().getEmail(), app.getJob().getTitle(), app.getApplicant().getFullName());
        }
    }

    
    public JobApplicationResponse withdrawApplication(Long id, String email) {
        JobApplication app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getApplicant().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("You are not authorized to withdraw this application");
        }

        if (app.getStatus() == ApplicationStatus.WITHDRAWN || app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Application already " + app.getStatus());
        }

        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);


        emailService.sendApplicationStatusEmail(
            app.getApplicant().getEmail(),
            app.getJob().getTitle(),
            app.getApplicant().getFullName(), 
            ApplicationStatus.WITHDRAWN
        );

        return toResponse(app);
    }





}
