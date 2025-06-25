package com.catalog.mapper;

import com.catalog.dto.ProductImageDto;
import com.catalog.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductImageMapper {
    
    public ProductImageDto toDto(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        
        return new ProductImageDto(
            productImage.getId(),
            productImage.getImageUrl(),
            productImage.getOriginalFilename(),
            productImage.getAltText(),
            productImage.getDisplayOrder(),
            productImage.getIsPrimary(),
            productImage.getFileSize(),
            productImage.getContentType(),
            productImage.getCreatedAt(),
            productImage.getUpdatedAt()
        );
    }
    
    public ProductImage toEntity(ProductImageDto productImageDto) {
        if (productImageDto == null) {
            return null;
        }
        
        ProductImage productImage = new ProductImage();
        productImage.setId(productImageDto.getId());
        productImage.setImageUrl(productImageDto.getImageUrl());
        productImage.setOriginalFilename(productImageDto.getOriginalFilename());
        productImage.setAltText(productImageDto.getAltText());
        productImage.setDisplayOrder(productImageDto.getDisplayOrder());
        productImage.setIsPrimary(productImageDto.getIsPrimary());
        productImage.setFileSize(productImageDto.getFileSize());
        productImage.setContentType(productImageDto.getContentType());
        productImage.setCreatedAt(productImageDto.getCreatedAt());
        productImage.setUpdatedAt(productImageDto.getUpdatedAt());
        
        return productImage;
    }
    
    public List<ProductImageDto> toDtoList(List<ProductImage> productImages) {
        if (productImages == null) {
            return null;
        }
        
        return productImages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductImage> toEntityList(List<ProductImageDto> productImageDtos) {
        if (productImageDtos == null) {
            return null;
        }
        
        return productImageDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}