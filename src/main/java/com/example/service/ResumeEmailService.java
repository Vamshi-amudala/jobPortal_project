package com.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ResumeEmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send an email with optional attachment.
     *
     * @param toEmail      recipient email address
     * @param subject      subject of the email
     * @param htmlContent  email body in HTML format
     * @param applicantName (optional) applicant's name
     * @param status       application status (used to determine attachment)
     */
    public void sendResume(String toEmail, String subject, String htmlContent, String applicantName, Object status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // If we want to send attachment, set multipart = true
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);  // true = HTML content

            // Attach offer letter only if status is SELECTED
            if (status != null && status.toString().equalsIgnoreCase("SELECTED")) {
                // Create offer letter content dynamically or fetch from storage
                String offerLetterContent = createOfferLetter(applicantName, subject);

                // Convert String content to InputStreamSource for attachment
                InputStreamSource attachmentSource = new InputStreamSource() {
                    @Override
                    public InputStream getInputStream() {
                        return new ByteArrayInputStream(offerLetterContent.getBytes(StandardCharsets.UTF_8));
                    }
                };

                
                helper.addAttachment("OfferLetter.txt", attachmentSource);
            }

            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Generate a simple offer letter content.
     * You can replace this with a real PDF generation logic if needed.
     */
    private String createOfferLetter(String applicantName, String subject) {
        return "Dear " + applicantName + ",\n\n"
            + "We are delighted to extend to you an offer to join our team at Our Company. "
            + "Your skills and experience stood out, and we believe you will make a valuable contribution to our continued success.\n\n"
            + "Below are the details of your offer:\n"
            + subject + "\n\n"
            + "Please review this offer carefully. If you accept the terms, kindly sign and return this letter by [insert deadline date]. "
            + "Should you have any questions or require further clarification, feel free to reach out to our HR department at hr@company.com.\n\n"
            + "We are excited about the prospect of working together and confident that you will thrive in this role.\n\n"
            + "Thank you for considering this opportunity with us.\n\n"
            + "Best regards,\n"
            + "Recruitment Team\n"
;
    }

}
