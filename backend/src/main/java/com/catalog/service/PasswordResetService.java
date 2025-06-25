package com.catalog.service;

import com.catalog.entity.PasswordResetToken;
import com.catalog.entity.User;
import com.catalog.repository.PasswordResetTokenRepository;
import com.catalog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${catalog.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${catalog.password-reset.max-attempts:5}")
    private int maxAttemptsPerHour;
    
    @Value("${catalog.app.base-url}")
    private String baseUrl;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public boolean initiatePasswordReset(String email) {
        try {
            logger.info("Initiating password reset for email: {}", email);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                logger.warn("Password reset requested for non-existent email: {}", email);
                // Return true to prevent email enumeration attacks
                return true;
            }
            
            User user = userOpt.get();
            
            // Check rate limiting
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentAttempts = tokenRepository.countByUserAndCreatedAtAfter(user, oneHourAgo);
            
            if (recentAttempts >= maxAttemptsPerHour) {
                logger.warn("Too many password reset attempts for user: {} ({})", user.getEmail(), email);
                throw new RuntimeException("Too many password reset attempts. Please try again later.");
            }
            
            // Invalidate existing tokens
            tokenRepository.markAllUserTokensAsUsed(user);
            
            // Generate new token
            String token = generateSecureToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);
            
            PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
            tokenRepository.save(resetToken);
            
            // Send email asynchronously
            String resetLink = baseUrl + "/reset-password?token=" + token;
            sendPasswordResetEmailAsync(user.getEmail(), user.getFirstName(), resetLink);
            
            logger.info("Password reset token created for user: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error initiating password reset for email: {}", email, e);
            throw new RuntimeException("Failed to initiate password reset: " + e.getMessage());
        }
    }
    
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        try {
            logger.info("Attempting password reset with token");
            
            Optional<PasswordResetToken> tokenOpt = tokenRepository.findValidToken(token, LocalDateTime.now());
            if (!tokenOpt.isPresent()) {
                logger.warn("Invalid or expired password reset token used");
                return false;
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            User user = resetToken.getUser();
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            
            // Invalidate all other tokens for this user
            tokenRepository.markAllUserTokensAsUsed(user);
            
            // Send confirmation email asynchronously
            sendPasswordResetConfirmationAsync(user.getEmail(), user.getFirstName());
            
            logger.info("Password reset successful for user: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            throw new RuntimeException("Failed to reset password: " + e.getMessage());
        }
    }
    
    public boolean validateResetToken(String token) {
        try {
            Optional<PasswordResetToken> tokenOpt = tokenRepository.findValidToken(token, LocalDateTime.now());
            boolean isValid = tokenOpt.isPresent();
            
            if (isValid) {
                logger.debug("Password reset token is valid");
            } else {
                logger.warn("Invalid or expired password reset token validation attempt");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error validating reset token", e);
            return false;
        }
    }
    
    @Async
    public void sendPasswordResetEmailAsync(String email, String userName, String resetLink) {
        try {
            emailService.sendPasswordResetEmail(email, userName, resetLink);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
        }
    }
    
    @Async
    public void sendPasswordResetConfirmationAsync(String email, String userName) {
        try {
            emailService.sendPasswordResetConfirmationEmail(email, userName);
        } catch (Exception e) {
            logger.error("Failed to send password reset confirmation email to: {}", email, e);
        }
    }
    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    // Cleanup expired tokens every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            logger.debug("Cleaning up expired password reset tokens");
            tokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error cleaning up expired tokens", e);
        }
    }
}