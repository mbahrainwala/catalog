package com.catalog.service;

import com.catalog.entity.User;
import com.catalog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserFileService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserFileService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${catalog.user.upload.path:uploads/users/}")
    private String uploadPath;
    
    @Value("${catalog.user.base-url:/uploads/}")
    private String baseUrl;
    
    @Transactional
    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        try {
            logger.info("Uploading profile picture for user: {}", userId);
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                throw new IllegalArgumentException("User not found");
            }
            
            User user = userOpt.get();
            
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("No file provided");
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 5MB");
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = "profile";
            }
            
            String extension = originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String filename = "profile_" + userId + "_" + UUID.randomUUID().toString() + extension;
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user profile picture URL
            String imageUrl = baseUrl + filename;
            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);
            
            logger.info("Profile picture uploaded successfully for user: {}", userId);
            return imageUrl;
            
        } catch (Exception e) {
            logger.error("Error uploading profile picture for user {}", userId, e);
            throw e;
        }
    }
    
    @Transactional
    public Map<String, String> uploadIdDocument1(Long userId, MultipartFile file) throws IOException {
        return uploadIdDocument(userId, file, "document1");
    }
    
    @Transactional
    public Map<String, String> uploadIdDocument2(Long userId, MultipartFile file) throws IOException {
        return uploadIdDocument(userId, file, "document2");
    }
    
    private Map<String, String> uploadIdDocument(Long userId, MultipartFile file, String documentType) throws IOException {
        try {
            logger.info("Uploading ID {} for user: {}", documentType, userId);
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                throw new IllegalArgumentException("User not found");
            }
            
            User user = userOpt.get();
            
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("No file provided");
            }
            
            // Check file size (10MB limit for documents)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 10MB");
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = "document";
            }
            
            String extension = originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String filename = documentType + "_" + userId + "_" + UUID.randomUUID().toString() + extension;
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user document URLs
            String documentUrl = baseUrl + filename;
            if ("document1".equals(documentType)) {
                user.setIdDocument1Url(documentUrl);
                user.setIdDocument1Filename(originalFilename);
            } else {
                user.setIdDocument2Url(documentUrl);
                user.setIdDocument2Filename(originalFilename);
            }
            
            userRepository.save(user);
            
            Map<String, String> result = new HashMap<>();
            result.put("documentUrl", documentUrl);
            result.put("filename", originalFilename);
            
            logger.info("ID {} uploaded successfully for user: {}", documentType, userId);
            return result;
            
        } catch (Exception e) {
            logger.error("Error uploading ID {} for user {}", documentType, userId, e);
            throw e;
        }
    }
}