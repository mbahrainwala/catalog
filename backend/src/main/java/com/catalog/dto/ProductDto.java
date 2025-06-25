package com.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductDto {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String primaryImageUrl; // Primary image URL from uploaded images
    private Boolean inStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, List<String>> filterValues;
    private List<ProductImageDto> images; // Add images list
    
    public ProductDto() {}
    
    public ProductDto(Long id, String name, String description, BigDecimal price, String category,
                     Boolean inStock, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.inStock = inStock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }
    
    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }
    
    public Boolean getInStock() {
        return inStock;
    }
    
    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Map<String, List<String>> getFilterValues() {
        return filterValues;
    }
    
    public void setFilterValues(Map<String, List<String>> filterValues) {
        this.filterValues = filterValues;
    }
    
    public List<ProductImageDto> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImageDto> images) {
        this.images = images;
    }
}