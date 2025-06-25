package com.catalog.mapper;

import com.catalog.dto.CategoryDto;
import com.catalog.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getDisplayOrder(),
            category.getActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
    
    public Category toEntity(CategoryDto categoryDto) {
        if (categoryDto == null) {
            return null;
        }
        
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setDisplayOrder(categoryDto.getDisplayOrder());
        category.setActive(categoryDto.getActive());
        category.setCreatedAt(categoryDto.getCreatedAt());
        category.setUpdatedAt(categoryDto.getUpdatedAt());
        
        return category;
    }
    
    public List<CategoryDto> toDtoList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<Category> toEntityList(List<CategoryDto> categoryDtos) {
        if (categoryDtos == null) {
            return null;
        }
        
        return categoryDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}