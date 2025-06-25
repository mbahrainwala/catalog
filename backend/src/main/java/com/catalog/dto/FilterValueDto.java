package com.catalog.dto;

public class FilterValueDto {
    
    private Long id;
    private String value;
    private String displayValue;
    private Integer displayOrder;
    private Boolean active;
    
    public FilterValueDto() {}
    
    public FilterValueDto(Long id, String value, String displayValue, Integer displayOrder, Boolean active) {
        this.id = id;
        this.value = value;
        this.displayValue = displayValue;
        this.displayOrder = displayOrder;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
    
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
}