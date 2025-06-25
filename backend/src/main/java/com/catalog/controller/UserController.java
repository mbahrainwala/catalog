package com.catalog.controller;

import com.catalog.dto.UserDto;
import com.catalog.entity.User;
import com.catalog.mapper.UserMapper;
import com.catalog.security.UserPrincipal;
import com.catalog.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userService.getUserById(userDetails.getId());
            
            if (!userOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User user = userOpt.get();
            UserDto userDto = userMapper.toDto(user);
            
            return ResponseEntity.ok(userDto);
            
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "An error occurred while fetching profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}