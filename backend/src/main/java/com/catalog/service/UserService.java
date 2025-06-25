package com.catalog.service;

import com.catalog.entity.User;
import com.catalog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public User createUser(String email, String firstName, String lastName, User.Role role) {
        try {
            logger.info("Creating new user: {}", email);
            
            // Check if user already exists
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("User with this email already exists");
            }
            
            // Generate temporary password
            String temporaryPassword = generateTemporaryPassword();
            
            // Create user
            User user = new User(email, passwordEncoder.encode(temporaryPassword), firstName, lastName);
            user.setRole(role);
            user.setIsTemporaryPassword(true);
            user.setAccountActivated(false);
            user.setActivationDeadline(LocalDateTime.now().plusHours(48));
            
            User savedUser = userRepository.save(user);
            
            // Send activation email
            try {
                emailService.sendAccountActivationEmail(email, firstName, email, temporaryPassword);
            } catch (Exception e) {
                logger.error("Failed to send activation email to: {}", email, e);
                // Don't fail user creation if email fails
            }
            
            logger.info("User created successfully: {}", email);
            return savedUser;
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", email, e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }
    
    @Transactional
    public User updateUserRole(Long id, User.Role role) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Prevent changing the last OWNER to a different role
                if (user.getRole() == User.Role.OWNER && role != User.Role.OWNER) {
                    long ownerCount = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == User.Role.OWNER)
                            .count();
                    
                    if (ownerCount <= 1) {
                        throw new RuntimeException("Cannot change the role of the last OWNER user");
                    }
                }
                
                user.setRole(role);
                return userRepository.save(user);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating user role for user {}", id, e);
            throw new RuntimeException("Failed to update user role: " + e.getMessage());
        }
    }
    
    @Transactional
    public User updateUserStatus(Long id, Boolean enabled) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Prevent disabling the last OWNER
                if (user.getRole() == User.Role.OWNER && !enabled) {
                    long enabledOwnerCount = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == User.Role.OWNER && u.getEnabled())
                            .count();
                    
                    if (enabledOwnerCount <= 1) {
                        throw new RuntimeException("Cannot disable the last enabled OWNER user");
                    }
                }
                
                user.setEnabled(enabled);
                return userRepository.save(user);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating user status for user {}", id, e);
            throw new RuntimeException("Failed to update user status: " + e.getMessage());
        }
    }
    
    @Transactional
    public boolean deleteUser(Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Prevent deleting the last OWNER
                if (user.getRole() == User.Role.OWNER) {
                    long ownerCount = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == User.Role.OWNER)
                            .count();
                    
                    if (ownerCount <= 1) {
                        throw new RuntimeException("Cannot delete the last OWNER user");
                    }
                }
                
                userRepository.delete(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting user {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    
    @Transactional
    public String resetUserPassword(Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Generate new temporary password
                String temporaryPassword = generateTemporaryPassword();
                
                // Update user
                user.setPassword(passwordEncoder.encode(temporaryPassword));
                user.setIsTemporaryPassword(true);
                user.setAccountActivated(false);
                user.setActivationDeadline(LocalDateTime.now().plusHours(48));
                
                userRepository.save(user);
                
                // Send activation email
                try {
                    emailService.sendAccountActivationEmail(user.getEmail(), user.getFirstName(), user.getEmail(), temporaryPassword);
                } catch (Exception e) {
                    logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
                    // Don't fail the operation if email fails
                }
                
                return temporaryPassword;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error resetting password for user {}", id, e);
            throw new RuntimeException("Failed to reset password: " + e.getMessage());
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
}