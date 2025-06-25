package com.catalog.mapper;

import com.catalog.dto.ProductDto;
import com.catalog.dto.ProductImageDto;
import com.catalog.entity.Product;
import com.catalog.entity.ProductImage;
import com.catalog.service.ProductFilterService;
import com.catalog.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    
    @Autowired
    private ProductFilterService productFilterService;
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private ProductImageMapper productImageMapper;
    
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getRating(),
            product.getInStock(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
        
        // Add filter values if product has an ID (i.e., it's persisted)
        if (product.getId() != null) {
            try {
                Map<String, List<String>> filterValues = productFilterService.getProductFilterValues(product.getId());
                dto.setFilterValues(filterValues);
            } catch (Exception e) {
                System.err.println("Error fetching filter values for product " + product.getId() + ": " + e.getMessage());
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
                System.err.println("Error fetching images for product " + product.getId() + ": " + e.getMessage());
            }
        }
        
        return dto;
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
        product.setCategory(productDto.getCategory());
        product.setRating(productDto.getRating());
        product.setInStock(productDto.getInStock());
        product.setCreatedAt(productDto.getCreatedAt());
        product.setUpdatedAt(productDto.getUpdatedAt());
        
        return product;
    }
    
    public List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<Product> toEntityList(List<ProductDto> productDtos) {
        if (productDtos == null) {
            return null;
        }
        
        return productDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}