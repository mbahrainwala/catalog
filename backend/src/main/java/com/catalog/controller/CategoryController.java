package com.catalog.controller;

import com.catalog.entity.Category;
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
    
    @GetMapping
    public ResponseEntity<List<Category>> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/names")
    public ResponseEntity<List<String>> getCategoryNames() {
        List<String> categoryNames = categoryService.getCategoryNames();
        return ResponseEntity.ok(categoryNames);
    }
}