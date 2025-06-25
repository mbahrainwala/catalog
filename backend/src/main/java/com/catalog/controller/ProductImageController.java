package com.catalog.controller;

import com.catalog.dto.ProductImageDto;
import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.mapper.ProductImageMapper;
import com.catalog.service.ProductImageService;
import com.catalog.service.ProductService;
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
            System.out.println("GET /api/products/" + productId + "/images (public)");
            
            // Verify product exists first
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                System.err.println("Product not found: " + productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get images
            List<ProductImage> images = productImageService.getProductImages(productId);
            List<ProductImageDto> imageDtos = productImageMapper.toDtoList(images);
            
            System.out.println("Successfully returning " + imageDtos.size() + " images for product " + productId);
            
            return ResponseEntity.ok(imageDtos);
            
        } catch (Exception e) {
            System.err.println("Error fetching product images for product " + productId + ": " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("GET /api/admin/products/" + productId + "/images");
            
            // Verify product exists first
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                System.err.println("Product not found: " + productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get fresh data from database
            List<ProductImage> images = productImageService.getProductImages(productId);
            List<ProductImageDto> imageDtos = productImageMapper.toDtoList(images);
            
            System.out.println("Successfully returning " + imageDtos.size() + " images for product " + productId);
            
            return ResponseEntity.ok(imageDtos);
            
        } catch (Exception e) {
            System.err.println("Error fetching product images for product " + productId + ": " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("POST /api/admin/products/" + productId + "/images");
            System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("File size: " + (file != null ? file.getSize() : "null"));
            System.out.println("Content type: " + (file != null ? file.getContentType() : "null"));
            System.out.println("Alt text: " + altText);
            System.out.println("Display order: " + displayOrder);
            System.out.println("Is primary: " + isPrimary);
            
            // Verify product exists
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                System.err.println("Product not found: " + productId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Validate file
            if (file == null || file.isEmpty()) {
                System.err.println("No file provided");
                Map<String, String> response = new HashMap<>();
                response.put("message", "No file provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Save the image (this will commit to database immediately due to @Transactional)
            ProductImage savedImage = productImageService.saveProductImage(
                productOpt.get(), file, altText, displayOrder, isPrimary);
            
            // Convert to DTO before returning
            ProductImageDto imageDto = productImageMapper.toDto(savedImage);
            
            System.out.println("Image saved successfully with ID: " + savedImage.getId());
            System.out.println("Image URL: " + savedImage.getImageUrl());
            System.out.println("Transaction will be committed automatically");
            
            // Return the saved image DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(imageDto);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            System.err.println("Unexpected error during image upload: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("PUT /api/admin/products/" + productId + "/images/" + imageId);
            
            ProductImage updatedImage = productImageService.updateProductImage(imageId, altText, displayOrder, isPrimary);
            if (updatedImage != null) {
                // Convert to DTO before returning
                ProductImageDto imageDto = productImageMapper.toDto(updatedImage);
                
                System.out.println("Image updated successfully, transaction will be committed");
                return ResponseEntity.ok(imageDto);
            } else {
                System.err.println("Image not found: " + imageId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error updating image: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("PUT /api/admin/products/" + productId + "/images/" + imageId + "/primary");
            
            productImageService.setPrimaryImage(productId, imageId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Primary image updated successfully");
            
            System.out.println("Primary image set successfully, transaction will be committed");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error setting primary image: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            System.err.println("Error setting primary image: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DELETE /api/admin/products/" + productId + "/images/" + imageId);
            
            boolean deleted = productImageService.deleteProductImage(imageId);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image deleted successfully");
                System.out.println("Image deleted successfully, transaction will be committed");
                return ResponseEntity.ok(response);
            } else {
                System.err.println("Image not found: " + imageId);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting image: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Internal server error during deletion");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}