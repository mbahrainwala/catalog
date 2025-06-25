package com.catalog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateAccountRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    public ActivateAccountRequest() {}
    
    public ActivateAccountRequest(String email, String temporaryPassword, String newPassword, String confirmPassword) {
        this.email = email;
        this.temporaryPassword = temporaryPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTemporaryPassword() {
        return temporaryPassword;
    }
    
    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}