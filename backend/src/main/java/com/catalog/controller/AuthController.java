package com.catalog.controller;

import com.catalog.dto.ActivateAccountRequest;
import com.catalog.dto.ChangePasswordRequest;
import com.catalog.dto.ForgotPasswordRequest;
import com.catalog.dto.JwtResponse;
import com.catalog.dto.LoginRequest;
import com.catalog.dto.ResetPasswordRequest;
import com.catalog.entity.User;
import com.catalog.repository.UserRepository;
import com.catalog.security.JwtUtils;
import com.catalog.security.UserPrincipal;
import com.catalog.service.PasswordResetService;
import com.catalog.service.UserActivationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    PasswordResetService passwordResetService;
    
    @Autowired
    UserActivationService userActivationService;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Authentication attempt for user: {}", loginRequest.getEmail());
            
            // Check if user exists and needs activation
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Check if account needs activation
                if (user.needsActivation()) {
                    if (user.isActivationExpired()) {
                        // Delete expired account
                        userRepository.delete(user);
                        response.put("message", "Account activation deadline has expired. Please contact your administrator to create a new account.");
                        return ResponseEntity.badRequest().body(response);
                    } else {
                        // Account needs activation
                        response.put("message", "Account requires activation. Please check your email for activation instructions.");
                        response.put("needsActivation", true);
                        response.put("email", loginRequest.getEmail());
                        return ResponseEntity.status(202).body(response); // 202 Accepted
                    }
                }
            }
            
            // Authenticate with Spring Security - it will handle password hashing internally
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            
            logger.info("Authentication successful for user: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getAuthorities().iterator().next().getAuthority()));
                    
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            response.put("message", "Invalid email or password");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/activate-account")
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountRequest activateRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate passwords match
            if (!activateRequest.getNewPassword().equals(activateRequest.getConfirmPassword())) {
                response.put("message", "New password and confirm password do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = userActivationService.activateAccount(
                activateRequest.getEmail(),
                activateRequest.getTemporaryPassword(),
                activateRequest.getNewPassword()
            );
            
            if (success) {
                response.put("message", "Account activated successfully! You can now log in with your new password.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Failed to activate account");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (RuntimeException e) {
            logger.warn("Account activation failed: {}", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error activating account", e);
            response.put("message", "An error occurred while activating your account");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/check-auth")
    public ResponseEntity<?> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse("",
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getAuthorities().iterator().next().getAuthority()));
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(userDetails.getId());
            
            if (!userOpt.isPresent()) {
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            
            // Verify current password
            if (!encoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                response.put("message", "Current password is incorrect");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify new password and confirm password match
            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                response.put("message", "New password and confirm password do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if new password is different from current password
            if (encoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
                response.put("message", "New password must be different from current password");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update password and clear temporary password flag
            user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
            user.setIsTemporaryPassword(false);
            userRepository.save(user);
            
            logger.info("Password changed successfully for user: {}", user.getEmail());
            
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error changing password", e);
            response.put("message", "An error occurred while changing password");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            logger.info("Forgot password request for email: {} with identity verification", forgotPasswordRequest.getEmail());
            
            boolean success = passwordResetService.initiatePasswordReset(
                forgotPasswordRequest.getEmail(),
                forgotPasswordRequest.getFirstName(),
                forgotPasswordRequest.getLastName()
            );
            
            if (success) {
                response.put("message", "If an account with the provided information exists, we've sent a password reset link to the email address.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Failed to process password reset request");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (RuntimeException e) {
            logger.warn("Password reset request failed: {}", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error processing forgot password request", e);
            response.put("message", "An error occurred while processing your request");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            logger.info("Password reset attempt with token");
            
            // Validate passwords match
            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
                response.put("message", "New password and confirm password do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = passwordResetService.resetPassword(
                resetPasswordRequest.getToken(), 
                resetPasswordRequest.getNewPassword()
            );
            
            if (success) {
                response.put("message", "Password reset successfully. You can now log in with your new password.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid or expired reset token");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            response.put("message", "An error occurred while resetting your password");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("message", "Token is valid");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid or expired token");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error validating reset token", e);
            response.put("valid", false);
            response.put("message", "Error validating token");
            return ResponseEntity.status(500).body(response);
        }
    }
}