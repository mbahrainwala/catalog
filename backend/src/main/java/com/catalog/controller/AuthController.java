package com.catalog.controller;

import com.catalog.dto.ChangePasswordRequest;
import com.catalog.dto.JwtResponse;
import com.catalog.dto.LoginRequest;
import com.catalog.dto.SignupRequest;
import com.catalog.entity.User;
import com.catalog.repository.UserRepository;
import com.catalog.security.JwtUtils;
import com.catalog.security.UserPrincipal;
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
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getAuthorities().iterator().next().getAuthority()));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        Map<String, String> response = new HashMap<>();
        
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            response.put("message", "Error: Username is already taken!");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            response.put("message", "Error: Email is already in use!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName());
        
        user.setRole(User.Role.USER);
        userRepository.save(user);
        
        response.put("message", "User registered successfully!");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/check-auth")
    public ResponseEntity<?> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse("",
                    userDetails.getId(),
                    userDetails.getUsername(),
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
            
            // Update password
            user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);
            
            logger.info("Password changed successfully for user: {}", user.getUsername());
            
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error changing password", e);
            response.put("message", "An error occurred while changing password");
            return ResponseEntity.status(500).body(response);
        }
    }
}