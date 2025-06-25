package com.catalog.dto;

import java.util.List;

public class FilterDto {
    
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Integer displayOrder;
    private Boolean active;
    private List<FilterValueDto> filterValues;
    
    public FilterDto() {}
    
    public FilterDto(Long id, String name, String displayName, String description, 
                    Integer displayOrder, Boolean active, List<FilterValueDto> filterValues) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
        this.filterValues = filterValues;
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
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public List<FilterValueDto> getFilterValues() {
        return filterValues;
    }
    
    public void setFilterValues(List<FilterValueDto> filterValues) {
        this.filterValues = filterValues;
    }
}