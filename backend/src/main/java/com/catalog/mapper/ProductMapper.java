package com.catalog.mapper;

import com.catalog.dto.ProductDto;
import com.catalog.entity.Product;
import com.catalog.service.ProductFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    
    @Autowired
    private ProductFilterService productFilterService;
    
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
            product.getImageUrl(),
            product.getRating(),
            product.getInStock(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
        
        // Add filter values if product has an ID (i.e., it's persisted)
        if (product.getId() != null) {
            dto.setFilterValues(productFilterService.getProductFilterValues(product.getId()));
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
        product.setImageUrl(productDto.getImageUrl());
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