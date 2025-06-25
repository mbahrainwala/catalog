package com.catalog.mapper;

import com.catalog.dto.ProductDto;
import com.catalog.dto.ProductImageDto;
import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.service.ProductFilterService;
import com.catalog.service.ProductImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductMapper.class);
    
    @Autowired
    private ProductFilterService productFilterService;
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private ProductImageMapper productImageMapper;
    
    public ProductDto toDto(Product product) {
        return toDto(product, false);
    }
    
    public ProductDto toDto(Product product, boolean includeAdminFields) {
        if (product == null) {
            return null;
        }
        
        // Check if current user is admin
        boolean isAdmin = includeAdminFields || isCurrentUserAdmin();
        
        ProductDto dto = new ProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getInStock(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
        
        // Only include cost price and margins for admin users
        if (isAdmin && product.getCostPrice() != null) {
            dto.setCostPrice(product.getCostPrice());
            dto.calculateMargins();
        }
        
        // Add filter values if product has an ID (i.e., it's persisted)
        if (product.getId() != null) {
            try {
                Map<String, List<String>> filterValues = productFilterService.getProductFilterValues(product.getId());
                dto.setFilterValues(filterValues);
            } catch (Exception e) {
                logger.error("Error fetching filter values for product {}", product.getId(), e);
            }
            
            try {
                // Get product images
                List<ProductImage> images = productImageService.getProductImages(product.getId());
                if (!images.isEmpty()) {
                    List<ProductImageDto> imageDtos = productImageMapper.toDtoList(images);
                    dto.setImages(imageDtos);
                    
                    // Set primary image URL
                    Optional<ProductImage> primaryImage = productImageService.getPrimaryImage(product.getId());
                    if (primaryImage.isPresent()) {
                        dto.setPrimaryImageUrl(primaryImage.get().getImageUrl());
                    } else if (!images.isEmpty()) {
                        // If no primary image is set, use the first image
                        dto.setPrimaryImageUrl(images.get(0).getImageUrl());
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching images for product {}", product.getId(), e);
            }
        }
        
        return dto;
    }
    
    public ProductDto toDtoForAdmin(Product product) {
        return toDto(product, true);
    }
    
    public Product toEntity(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setId(productDto.getId());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCostPrice(productDto.getCostPrice());
        product.setCategory(productDto.getCategory());
        product.setInStock(productDto.getInStock());
        product.setCreatedAt(productDto.getCreatedAt());
        product.setUpdatedAt(productDto.getUpdatedAt());
        
        return product;
    }
    
    public List<ProductDto> toDtoList(List<Product> products) {
        return toDtoList(products, false);
    }
    
    public List<ProductDto> toDtoList(List<Product> products, boolean includeAdminFields) {
        if (products == null) {
            return null;
        }
        
        return products.stream()
                .map(product -> toDto(product, includeAdminFields))
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> toDtoListForAdmin(List<Product> products) {
        return toDtoList(products, true);
    }
    
    public List<Product> toEntityList(List<ProductDto> productDtos) {
        if (productDtos == null) {
            return null;
        }
        
        return productDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    private boolean isCurrentUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            }
        } catch (Exception e) {
            logger.debug("Error checking admin status", e);
        }
        return false;
    }
}