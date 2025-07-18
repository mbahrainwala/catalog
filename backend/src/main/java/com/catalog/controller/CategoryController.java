package com.catalog.controller;

import com.catalog.dto.CategoryDto;
import com.catalog.entity.Category;
import com.catalog.mapper.CategoryMapper;
import com.catalog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        return ResponseEntity.ok(categoryDtos);
    }
    
    @GetMapping("/names")
    public ResponseEntity<List<String>> getCategoryNames() {
        List<String> categoryNames = categoryService.getCategoryNames();
        return ResponseEntity.ok(categoryNames);
    }
}