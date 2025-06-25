package com.catalog.controller;

import com.catalog.dto.ProductImageDto;
import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.mapper.ProductImageMapper;
import com.catalog.service.ProductImageService;
import com.catalog.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class ProductImageController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductImageMapper productImageMapper;
    
    // Public endpoint for viewing product images
    @GetMapping("/api/products/{productId}/images")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductImagesPublic(@PathVariable Long productId) {
        try {
            logger.debug("GET /api/products/{}/images (public)", productId);
            
            // Verify product exists first
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                logger.warn("Product not found: {}", productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get images
            List<ProductImage> images = productImageService.getProductImages(productId);
            List<ProductImageDto> imageDtos = productImageMapper.toDtoList(images);
            
            logger.debug("Successfully returning {} images for product {}", imageDtos.size(), productId);
            
            return ResponseEntity.ok(imageDtos);
            
        } catch (Exception e) {
            logger.error("Error fetching product images for product {}", productId, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error while fetching images");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Admin endpoints (require authentication)
    @GetMapping("/api/admin/products/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) {
        try {
            logger.debug("GET /api/admin/products/{}/images", productId);
            
            // Verify product exists first
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                logger.warn("Product not found: {}", productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get fresh data from database
            List<ProductImage> images = productImageService.getProductImages(productId);
            List<ProductImageDto> imageDtos = productImageMapper.toDtoList(images);
            
            logger.debug("Successfully returning {} images for product {}", imageDtos.size(), productId);
            
            return ResponseEntity.ok(imageDtos);
            
        } catch (Exception e) {
            logger.error("Error fetching product images for product {}", productId, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error while fetching images");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/api/admin/products/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        
        try {
            logger.info("POST /api/admin/products/{}/images", productId);
            logger.debug("File name: {}, File size: {}, Content type: {}, Alt text: {}, Display order: {}, Is primary: {}", 
                    file != null ? file.getOriginalFilename() : "null",
                    file != null ? file.getSize() : "null",
                    file != null ? file.getContentType() : "null",
                    altText, displayOrder, isPrimary);
            
            // Verify product exists
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                logger.warn("Product not found: {}", productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Validate file
            if (file == null || file.isEmpty()) {
                logger.warn("No file provided for product {}", productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "No file provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Save the image (this will commit to database immediately due to @Transactional)
            ProductImage savedImage = productImageService.saveProductImage(
                productOpt.get(), file, altText, displayOrder, isPrimary);
            
            // Convert to DTO before returning
            ProductImageDto imageDto = productImageMapper.toDto(savedImage);
            
            logger.info("Image saved successfully with ID: {}, URL: {}", savedImage.getId(), savedImage.getImageUrl());
            
            // Return the saved image DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(imageDto);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error during image upload for product {}: {}", productId, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during image upload for product {}", productId, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error during upload");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/api/admin/products/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        
        try {
            logger.info("PUT /api/admin/products/{}/images/{}", productId, imageId);
            
            ProductImage updatedImage = productImageService.updateProductImage(imageId, altText, displayOrder, isPrimary);
            if (updatedImage != null) {
                // Convert to DTO before returning
                ProductImageDto imageDto = productImageMapper.toDto(updatedImage);
                
                logger.info("Image updated successfully");
                return ResponseEntity.ok(imageDto);
            } else {
                logger.warn("Image not found: {}", imageId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error updating image {}", imageId, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error during update");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/api/admin/products/{productId}/images/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> setPrimaryImage(@PathVariable Long productId, @PathVariable Long imageId) {
        try {
            logger.info("PUT /api/admin/products/{}/images/{}/primary", productId, imageId);
            
            productImageService.setPrimaryImage(productId, imageId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Primary image updated successfully");
            
            logger.info("Primary image set successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error setting primary image for product {}, image {}: {}", productId, imageId, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            logger.error("Error setting primary image for product {}, image {}", productId, imageId, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error while setting primary image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/api/admin/products/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteProductImage(@PathVariable Long productId, @PathVariable Long imageId) {
        try {
            logger.info("DELETE /api/admin/products/{}/images/{}", productId, imageId);
            
            boolean deleted = productImageService.deleteProductImage(imageId);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image deleted successfully");
                logger.info("Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Image not found: {}", imageId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error deleting image {}", imageId, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error during deletion");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}