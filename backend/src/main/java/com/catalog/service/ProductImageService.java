package com.catalog.service;

import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.repository.ProductImageRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductImageService {
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Value("${catalog.image.upload.path:uploads/products/}")
    private String uploadPath;
    
    @Value("${catalog.image.base-url:/uploads/}")
    private String baseUrl;
    
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
    }
    
    public Optional<ProductImage> getPrimaryImage(Long productId) {
        return productImageRepository.findPrimaryImageByProductId(productId);
    }
    
    public ProductImage saveProductImage(Product product, MultipartFile file, String altText, Integer displayOrder, Boolean isPrimary) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Check if product already has 5 images
        long imageCount = productImageRepository.countByProductId(product.getId());
        if (imageCount >= 5) {
            throw new IllegalArgumentException("Product can have maximum 5 images");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create ProductImage entity
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(baseUrl + filename);
        productImage.setOriginalFilename(originalFilename);
        productImage.setAltText(altText);
        productImage.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        productImage.setFileSize(file.getSize());
        productImage.setContentType(contentType);
        
        // Handle primary image logic
        if (isPrimary != null && isPrimary) {
            setPrimaryImage(product.getId(), null); // Clear existing primary
            productImage.setIsPrimary(true);
        } else {
            // If this is the first image, make it primary
            if (imageCount == 0) {
                productImage.setIsPrimary(true);
            } else {
                productImage.setIsPrimary(false);
            }
        }
        
        return productImageRepository.save(productImage);
    }
    
    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {
        // Clear existing primary image
        productImageRepository.clearPrimaryImageForProduct(productId);
        
        // Set new primary image if imageId is provided
        if (imageId != null) {
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isPresent() && imageOpt.get().getProduct().getId().equals(productId)) {
                ProductImage image = imageOpt.get();
                image.setIsPrimary(true);
                productImageRepository.save(image);
            }
        }
    }
    
    public boolean deleteProductImage(Long imageId) {
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            ProductImage image = imageOpt.get();
            
            // Delete file from filesystem
            try {
                String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadPath).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but continue with database deletion
                System.err.println("Failed to delete file: " + e.getMessage());
            }
            
            // If this was the primary image, make another image primary
            if (image.getIsPrimary()) {
                List<ProductImage> otherImages = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(image.getProduct().getId());
                otherImages.removeIf(img -> img.getId().equals(imageId));
                if (!otherImages.isEmpty()) {
                    ProductImage newPrimary = otherImages.get(0);
                    newPrimary.setIsPrimary(true);
                    productImageRepository.save(newPrimary);
                }
            }
            
            productImageRepository.delete(image);
            return true;
        }
        return false;
    }
    
    public ProductImage updateProductImage(Long imageId, String altText, Integer displayOrder, Boolean isPrimary) {
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            ProductImage image = imageOpt.get();
            
            if (altText != null) {
                image.setAltText(altText);
            }
            
            if (displayOrder != null) {
                image.setDisplayOrder(displayOrder);
            }
            
            if (isPrimary != null && isPrimary && !image.getIsPrimary()) {
                setPrimaryImage(image.getProduct().getId(), imageId);
            }
            
            return productImageRepository.save(image);
        }
        return null;
    }
}