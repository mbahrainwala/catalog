package com.catalog.controller;

import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.service.ProductImageService;
import com.catalog.service.ProductService;
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
@RequestMapping("/api/admin/products/{productId}/images")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageController {
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long productId) {
        List<ProductImage> images = productImageService.getProductImages(productId);
        return ResponseEntity.ok(images);
    }
    
    @PostMapping
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Verify product exists
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                response.put("message", "Product not found");
                return ResponseEntity.notFound().build();
            }
            
            ProductImage savedImage = productImageService.saveProductImage(
                productOpt.get(), file, altText, displayOrder, isPrimary);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
            
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{imageId}")
    public ResponseEntity<?> updateProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            ProductImage updatedImage = productImageService.updateProductImage(imageId, altText, displayOrder, isPrimary);
            if (updatedImage != null) {
                return ResponseEntity.ok(updatedImage);
            } else {
                response.put("message", "Image not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("message", "Failed to update image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Long productId, @PathVariable Long imageId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            productImageService.setPrimaryImage(productId, imageId);
            response.put("message", "Primary image updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to set primary image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long productId, @PathVariable Long imageId) {
        Map<String, String> response = new HashMap<>();
        
        boolean deleted = productImageService.deleteProductImage(imageId);
        if (deleted) {
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Image not found");
            return ResponseEntity.notFound().build();
        }
    }
}