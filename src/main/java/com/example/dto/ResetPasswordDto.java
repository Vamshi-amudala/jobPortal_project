package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto {
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String otp;
    
    @NotBlank
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;


//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getOtp() { return otp; }
//    public void setOtp(String otp) { this.otp = otp; }
//
//    public String getNewPassword() { return newPassword; }
//    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
