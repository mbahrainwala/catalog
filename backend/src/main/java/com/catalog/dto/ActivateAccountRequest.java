package com.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateAccountRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    public ActivateAccountRequest() {}
    
    public ActivateAccountRequest(String username, String temporaryPassword, String newPassword, String confirmPassword) {
        this.username = username;
        this.temporaryPassword = temporaryPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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