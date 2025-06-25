package com.catalog.controller;

import com.catalog.dto.UserDto;
import com.catalog.entity.User;
import com.catalog.mapper.UserMapper;
import com.catalog.service.UserService;
import com.catalog.service.UserFileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/owner")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {
    
    private static final Logger logger = LoggerFactory.getLogger(OwnerController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFileService userFileService;
    
    @Autowired
    private UserMapper userMapper;
    
    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserDto> userDtos = userMapper.toDtoList(users);
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            logger.error("Error in getAllUsers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            return user.map(u -> ResponseEntity.ok(userMapper.toDto(u)))
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error in getUserById for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody Map<String, Object> userData) {
        try {
            logger.info("Creating user with data: {}", userData);
            
            String email = (String) userData.get("email");
            String firstName = (String) userData.get("firstName");
            String lastName = (String) userData.get("lastName");
            String roleStr = (String) userData.get("role");
            
            if (email == null || firstName == null || lastName == null || roleStr == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Email, firstName, lastName, and role are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            User.Role role;
            try {
                role = User.Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Invalid role. Must be USER, ADMIN, or OWNER");
                return ResponseEntity.badRequest().body(response);
            }
            
            User createdUser = userService.createUser(email, firstName, lastName, role);
            UserDto userDto = userMapper.toDto(createdUser);
            
            logger.info("User created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
            
        } catch (Exception e) {
            logger.error("Error creating user", e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        try {
            logger.info("Updating user {}: {}", id, userDto);
            
            User updatedUser = userService.updateUserProfile(id, userDto);
            if (updatedUser != null) {
                UserDto responseDto = userMapper.toDto(updatedUser);
                logger.info("User updated successfully");
                return ResponseEntity.ok(responseDto);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleData) {
        try {
            logger.info("Updating user role for user {}: {}", id, roleData);
            
            String roleStr = roleData.get("role");
            if (roleStr == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Role is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            User.Role role;
            try {
                role = User.Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Invalid role. Must be USER, ADMIN, or OWNER");
                return ResponseEntity.badRequest().body(response);
            }
            
            User updatedUser = userService.updateUserRole(id, role);
            if (updatedUser != null) {
                UserDto userDto = userMapper.toDto(updatedUser);
                logger.info("User role updated successfully");
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating user role for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update user role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> statusData) {
        try {
            logger.info("Updating user status for user {}: {}", id, statusData);
            
            Boolean enabled = statusData.get("enabled");
            if (enabled == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Enabled status is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            User updatedUser = userService.updateUserStatus(id, enabled);
            if (updatedUser != null) {
                UserDto userDto = userMapper.toDto(updatedUser);
                logger.info("User status updated successfully");
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating user status for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update user status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user with id: {}", id);
            
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                logger.info("User deleted successfully");
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error deleting user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id) {
        try {
            logger.info("Resetting password for user: {}", id);
            
            String temporaryPassword = userService.resetUserPassword(id);
            if (temporaryPassword != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password reset successfully. Temporary password sent to user's email.");
                response.put("temporaryPassword", temporaryPassword); // For admin reference
                
                logger.info("Password reset successfully for user: {}", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error resetting password for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // File upload endpoints
    @PostMapping("/users/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading profile picture for user: {}", id);
            
            String imageUrl = userFileService.uploadProfilePicture(id, file);
            if (imageUrl != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Profile picture uploaded successfully");
                response.put("imageUrl", imageUrl);
                
                logger.info("Profile picture uploaded successfully for user: {}", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error uploading profile picture for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to upload profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/users/{id}/id-document1")
    public ResponseEntity<?> uploadIdDocument1(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading ID document 1 for user: {}", id);
            
            Map<String, String> result = userFileService.uploadIdDocument1(id, file);
            if (result != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "ID document 1 uploaded successfully");
                response.putAll(result);
                
                logger.info("ID document 1 uploaded successfully for user: {}", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error uploading ID document 1 for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to upload ID document 1: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/users/{id}/id-document2")
    public ResponseEntity<?> uploadIdDocument2(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading ID document 2 for user: {}", id);
            
            Map<String, String> result = userFileService.uploadIdDocument2(id, file);
            if (result != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "ID document 2 uploaded successfully");
                response.putAll(result);
                
                logger.info("ID document 2 uploaded successfully for user: {}", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error uploading ID document 2 for user {}", id, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to upload ID document 2: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}