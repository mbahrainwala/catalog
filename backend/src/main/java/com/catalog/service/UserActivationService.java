package com.catalog.service;

import com.catalog.entity.User;
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
import java.util.List;

@Service
public class UserActivationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserActivationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${catalog.account.activation-hours:48}")
    private int activationHours;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public User createUserWithTemporaryPassword(String email, String firstName, String lastName) {
        try {
            logger.info("Creating user with temporary password: {}", email);
            
            // Generate temporary password
            String temporaryPassword = generateTemporaryPassword();
            
            // Create user
            User user = new User(email, passwordEncoder.encode(temporaryPassword), firstName, lastName);
            user.setRole(User.Role.USER);
            user.setIsTemporaryPassword(true);
            user.setAccountActivated(false);
            user.setActivationDeadline(LocalDateTime.now().plusHours(activationHours));
            
            User savedUser = userRepository.save(user);
            
            // Send activation email asynchronously
            sendActivationEmailAsync(email, firstName, email, temporaryPassword);
            
            logger.info("User created with temporary password: {}", email);
            return savedUser;
            
        } catch (Exception e) {
            logger.error("Error creating user with temporary password: {}", email, e);
            throw new RuntimeException("Failed to create user account: " + e.getMessage());
        }
    }
    
    @Transactional
    public boolean activateAccount(String email, String temporaryPassword, String newPassword) {
        try {
            logger.info("Attempting to activate account for user: {}", email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if account needs activation
            if (!user.needsActivation()) {
                throw new RuntimeException("Account is already activated or does not require activation");
            }
            
            // Check if activation deadline has passed
            if (user.isActivationExpired()) {
                logger.warn("Activation deadline expired for user: {}", email);
                // Delete expired user account
                userRepository.delete(user);
                throw new RuntimeException("Activation deadline has expired. Please create a new account.");
            }
            
            // Verify temporary password
            if (!passwordEncoder.matches(temporaryPassword, user.getPassword())) {
                throw new RuntimeException("Invalid temporary password");
            }
            
            // Update user with new password and activate account
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setIsTemporaryPassword(false);
            user.setAccountActivated(true);
            user.setActivationDeadline(null);
            
            userRepository.save(user);
            
            // Send confirmation email
            sendAccountActivatedEmailAsync(user.getEmail(), user.getFirstName());
            
            logger.info("Account activated successfully for user: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Error activating account for user: {}", email, e);
            throw new RuntimeException("Failed to activate account: " + e.getMessage());
        }
    }
    
    @Async
    public void sendActivationEmailAsync(String email, String firstName, String emailAddress, String temporaryPassword) {
        try {
            emailService.sendAccountActivationEmail(email, firstName, emailAddress, temporaryPassword);
        } catch (Exception e) {
            logger.error("Failed to send activation email to: {}", email, e);
        }
    }
    
    @Async
    public void sendAccountActivatedEmailAsync(String email, String firstName) {
        try {
            emailService.sendAccountActivatedEmail(email, firstName);
        } catch (Exception e) {
            logger.error("Failed to send account activated email to: {}", email, e);
        }
    }
    
    private String generateTemporaryPassword() {
        // Generate a secure 12-character temporary password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    // Clean up expired unactivated accounts every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredAccounts() {
        try {
            logger.debug("Cleaning up expired unactivated accounts");
            
            LocalDateTime now = LocalDateTime.now();
            List<User> expiredUsers = userRepository.findExpiredUnactivatedUsers(now);
            
            if (!expiredUsers.isEmpty()) {
                logger.info("Found {} expired unactivated accounts to delete", expiredUsers.size());
                
                for (User user : expiredUsers) {
                    logger.info("Deleting expired unactivated account: {} ({})", user.getEmail(), user.getEmail());
                    userRepository.delete(user);
                }
                
                logger.info("Cleaned up {} expired unactivated accounts", expiredUsers.size());
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired accounts", e);
        }
    }
}