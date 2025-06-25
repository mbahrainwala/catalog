package com.catalog.service;

import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.repository.ProductImageRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional // Make the entire service transactional
public class ProductImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductImageService.class);
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Value("${catalog.image.upload.path:uploads/products/}")
    private String uploadPath;
    
    @Value("${catalog.image.base-url:/uploads/}")
    private String baseUrl;
    
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        try {
            logger.debug("Fetching images for product ID: {}", productId);
            
            if (productId == null) {
                logger.warn("Product ID is null");
                return Collections.emptyList();
            }
            
            List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
            logger.debug("Found {} images for product {}", images.size(), productId);
            
            return images;
            
        } catch (Exception e) {
            logger.error("Error fetching product images for product {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductImage> getPrimaryImage(Long productId) {
        try {
            if (productId == null) {
                return Optional.empty();
            }
            return productImageRepository.findPrimaryImageByProductId(productId);
        } catch (Exception e) {
            logger.error("Error fetching primary image for product {}", productId, e);
            return Optional.empty();
        }
    }
    
    @Transactional
    public ProductImage saveProductImage(Product product, MultipartFile file, String altText, Integer displayOrder, Boolean isPrimary) throws IOException {
        try {
            logger.info("Starting image save process for product: {}", product.getId());
            
            // Validate inputs
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("Product is required");
            }
            
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            // Check file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 10MB");
            }
            
            // Check if product already has 5 images
            long imageCount = productImageRepository.countByProductId(product.getId());
            logger.debug("Current image count for product {}: {}", product.getId(), imageCount);
            
            if (imageCount >= 5) {
                throw new IllegalArgumentException("Product can have maximum 5 images");
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = "image";
            }
            
            String extension = originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            logger.debug("Generated filename: {}, Upload path: {}", filename, uploadPath);
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                logger.info("Creating upload directory: {}", uploadDir.toAbsolutePath());
                Files.createDirectories(uploadDir);
            }
            
            // Save file first
            Path filePath = uploadDir.resolve(filename);
            logger.debug("Saving file to: {}", filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create ProductImage entity
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(baseUrl + filename);
            productImage.setOriginalFilename(originalFilename);
            productImage.setAltText(altText != null && !altText.trim().isEmpty() ? altText : originalFilename);
            productImage.setDisplayOrder(displayOrder != null ? displayOrder : (int) imageCount);
            productImage.setFileSize(file.getSize());
            productImage.setContentType(contentType);
            
            // Handle primary image logic
            if (isPrimary != null && isPrimary) {
                logger.debug("Setting as primary image");
                // Clear existing primary images for this product
                productImageRepository.clearPrimaryImageForProduct(product.getId());
                productImage.setIsPrimary(true);
            } else {
                // If this is the first image, make it primary
                if (imageCount == 0) {
                    logger.debug("First image - setting as primary");
                    productImage.setIsPrimary(true);
                } else {
                    productImage.setIsPrimary(false);
                }
            }
            
            // Save to database
            ProductImage savedImage = productImageRepository.save(productImage);
            
            // Force flush to ensure database is updated immediately
            productImageRepository.flush();
            
            logger.info("Image saved successfully with ID: {}", savedImage.getId());
            
            return savedImage;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in saveProductImage: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("IO error in saveProductImage", e);
            throw new IOException("Failed to save image file: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error in saveProductImage", e);
            throw new RuntimeException("Failed to save product image: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {
        try {
            logger.info("Setting primary image for product {}, image ID: {}", productId, imageId);
            
            if (productId == null || imageId == null) {
                throw new IllegalArgumentException("Product ID and Image ID are required");
            }
            
            // Clear existing primary image
            productImageRepository.clearPrimaryImageForProduct(productId);
            
            // Set new primary image
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isPresent() && imageOpt.get().getProduct().getId().equals(productId)) {
                ProductImage image = imageOpt.get();
                image.setIsPrimary(true);
                productImageRepository.save(image);
                
                // Force flush to ensure database is updated immediately
                productImageRepository.flush();
                
                logger.info("Primary image set successfully");
            } else {
                throw new IllegalArgumentException("Image not found or doesn't belong to product");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in setPrimaryImage: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error setting primary image", e);
            throw new RuntimeException("Failed to set primary image: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public boolean deleteProductImage(Long imageId) {
        try {
            logger.info("Deleting image with ID: {}", imageId);
            
            if (imageId == null) {
                logger.warn("Image ID is null");
                return false;
            }
            
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isPresent()) {
                ProductImage image = imageOpt.get();
                Long productId = image.getProduct().getId();
                boolean wasPrimary = image.getIsPrimary();
                String imageUrl = image.getImageUrl();
                
                // Delete from database first
                productImageRepository.delete(image);
                
                // Force flush to ensure database is updated immediately
                productImageRepository.flush();
                
                logger.info("Image deleted from database successfully");
                
                // Delete file from filesystem (after database deletion)
                try {
                    if (imageUrl != null && imageUrl.contains("/")) {
                        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        Path filePath = Paths.get(uploadPath).resolve(filename);
                        boolean fileDeleted = Files.deleteIfExists(filePath);
                        logger.debug("File deletion result: {} for file: {}", fileDeleted, filePath);
                    }
                } catch (IOException e) {
                    // Log error but don't fail the operation since database deletion succeeded
                    logger.warn("Failed to delete file (database deletion succeeded)", e);
                }
                
                // If this was the primary image, make another image primary
                if (wasPrimary) {
                    logger.debug("Deleted image was primary, finding replacement");
                    List<ProductImage> otherImages = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
                    if (!otherImages.isEmpty()) {
                        ProductImage newPrimary = otherImages.get(0);
                        newPrimary.setIsPrimary(true);
                        productImageRepository.save(newPrimary);
                        
                        // Force flush to ensure database is updated immediately
                        productImageRepository.flush();
                        
                        logger.info("Set new primary image: {}", newPrimary.getId());
                    }
                }
                
                logger.info("Image deletion completed");
                return true;
            }
            
            logger.warn("Image not found with ID: {}", imageId);
            return false;
            
        } catch (Exception e) {
            logger.error("Error deleting image {}", imageId, e);
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public ProductImage updateProductImage(Long imageId, String altText, Integer displayOrder, Boolean isPrimary) {
        try {
            logger.info("Updating image with ID: {}", imageId);
            
            if (imageId == null) {
                logger.warn("Image ID is null");
                return null;
            }
            
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isPresent()) {
                ProductImage image = imageOpt.get();
                
                if (altText != null && !altText.trim().isEmpty()) {
                    image.setAltText(altText);
                }
                
                if (displayOrder != null) {
                    image.setDisplayOrder(displayOrder);
                }
                
                if (isPrimary != null && isPrimary && !image.getIsPrimary()) {
                    setPrimaryImage(image.getProduct().getId(), imageId);
                    // Refresh the image from database after setting primary
                    return productImageRepository.findById(imageId).orElse(image);
                }
                
                ProductImage updatedImage = productImageRepository.save(image);
                
                // Force flush to ensure database is updated immediately
                productImageRepository.flush();
                
                logger.info("Image updated successfully");
                return updatedImage;
            }
            
            logger.warn("Image not found with ID: {}", imageId);
            return null;
            
        } catch (Exception e) {
            logger.error("Error updating image {}", imageId, e);
            throw new RuntimeException("Failed to update image: " + e.getMessage(), e);
        }
    }
}