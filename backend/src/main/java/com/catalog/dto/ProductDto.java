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
    private BigDecimal costPrice; // Hidden field - only visible to admins
    private String category;
    private String primaryImageUrl; // Primary image URL from uploaded images
    private Boolean inStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, List<String>> filterValues;
    private List<ProductImageDto> images; // Add images list
    
    // Calculated fields for admin use
    private BigDecimal margin;
    private BigDecimal marginPercentage;
    
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
    
    // Helper methods
    public void calculateMargins() {
        if (costPrice != null && price != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.margin = price.subtract(costPrice);
            this.marginPercentage = margin.divide(costPrice, 4, java.math.RoundingMode.HALF_UP)
                                          .multiply(new BigDecimal("100"));
        }
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
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
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
    
    public BigDecimal getMargin() {
        return margin;
    }
    
    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }
    
    public BigDecimal getMarginPercentage() {
        return marginPercentage;
    }
    
    public void setMarginPercentage(BigDecimal marginPercentage) {
        this.marginPercentage = marginPercentage;
    }
}