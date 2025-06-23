package com.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.entity.ApplicationStatus;

import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void sendApplicationConfirmationEmail(String toEmail, String subject, String htmlBody, File fileAttachment) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // This tells JavaMail it's HTML content

            if (fileAttachment != null && fileAttachment.exists()) {
                helper.addAttachment(fileAttachment.getName(), fileAttachment);
            }

            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
        }
    }


    public void sendApplicationStatusEmail(String toEmail, String jobTitle, String candidateName, ApplicationStatus status) {
        String subject = "Application Update: " + jobTitle;
        String body;
        FileSystemResource attachment = null;

        switch (status) {
        case SELECTED:
            body = "Dear " + candidateName + ",\n\n" +
                   "Congratulations! We are pleased to inform you that you have been selected for the position of " + jobTitle + ".\n\n" +
                   "Your qualifications, experience, and enthusiasm made a strong impression on our hiring team. We believe you will be a valuable asset to our organization.\n\n" +
                   "Please find your official offer letter attached to this email. The letter contains important details about your role, compensation, and next steps.\n\n" +
                   "We kindly request that you review the offer and respond by the mentioned deadline. Should you have any questions or need further clarification, feel free to contact our HR department at hr@company.com.\n\n" +
                   "We are excited to have you on board and look forward to working together to achieve great things.\n\n" +
                   "Warm regards,\n" +
                   "Recruitment Team\n" +
                   "Your Company Name";                
                
                String filename = "Offer_Letter_" + candidateName + ".txt";
                String content = "Dear " + candidateName + ",\n\n" +
                        "We are pleased to offer you the position of " + jobTitle + ".\n\nWelcome aboard!\n\nRegards,\nTeam";
                try {
                    Path path = Paths.get(System.getProperty("java.io.tmpdir"), filename);
                    Files.write(path, content.getBytes());
                    attachment = new FileSystemResource(path.toFile());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create offer letter file", e);
                }
                break;
                
        case UNDER_REVIEW:
            body = "Dear " + candidateName + ",\n\n" +
                   "We are pleased to inform you that your application for the position of \"" + jobTitle + "\" is currently under review and has moved to the next stage of our hiring process.\n\n" +
                   "In the coming days, you will receive further instructions regarding the assessment and interview process. This may include an online test and a scheduled discussion with our technical team.\n\n" +
                   "To ensure a smooth experience, please be prepared with a quiet workspace, a stable internet connection, and availability during standard business hours.\n\n" +
                   "We appreciate your interest in the role and look forward to learning more about you.\n\n" +
                   "Warm regards,\n" +
                   "Recruitment Team\n" +
                   "JobPortal";
            break;

            

                
        case WITHDRAWN:
            body = "Dear " + candidateName + ",\n\n" +
                   "This is to confirm that your application for the position of " + jobTitle + " has been withdrawn as per your request.\n\n" +
                   "If this was done in error or you have any questions, please contact us at support@company.com.\n\n" +
                   "Thank you for your interest.\n\n" +
                   "Best regards,\n" +
                   "Recruitment Team";
            break;


        case REJECTED:
            body = "Dear " + candidateName + ",\n\n" +
                   "Thank you for your application for the position of " + jobTitle + ". We truly appreciate the time and effort you invested in the process.\n\n" +
                   "After careful review of your qualifications and interview, we regret to inform you that we have decided to proceed with another candidate for this role.\n\n" +
                   "This decision was not easy, as we had many strong applicants. We encourage you to apply for future opportunities with us that match your skills and experience.\n\n" +
                   "We wish you continued success in your career journey.\n\n" +
                   "Warm regards,\n" +
                   "Recruitment Team\n" ;
            break;

            default:
                throw new IllegalArgumentException("Unsupported status: " + status);
        }

        sendEmail(toEmail, subject, body, attachment);
    }

    private void sendEmail(String toEmail, String subject, String body, FileSystemResource attachment) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, attachment != null);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body,false);

            if (attachment != null) {
                helper.addAttachment(attachment.getFilename(), attachment);
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
  
    
    
 
    // Helper method for sending simple text emails (no attachment)
    private void sendSimpleEmail(String toEmail, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false); // no multipart

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Your Password Reset Code";
        String body = "Dear User,\n\n"
                    + "Your OTP for resetting your password is: " + otpCode + "\n"
                    + "This code will expire in 5 minutes.\n\n"
                    + "Best regards,\nTeam";

        sendEmail(toEmail, subject, body, null);
    }
    
    public void sendResetPasswordConfirmationEmail(String toEmail) {
        String subject = "Your Password Has Been Successfully Reset";
        String body = "Hello,\n\n"
                    + "This is a confirmation that your password has been successfully updated.\n\n"
                    + "If you did not perform this action, please contact our support team immediately.\n\n"
                    + "Thank you,\n"
                    + "Job Portal - Keep searching...";

       
        sendSimpleEmail(toEmail, subject, body);
    }


    public void sendSelectedEmail(String email, String jobTitle, String candidateName) {
        sendApplicationStatusEmail(email, jobTitle, candidateName, ApplicationStatus.SELECTED);
    }

}
