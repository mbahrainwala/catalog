package com.catalog.service;

import com.catalog.entity.Category;
import com.catalog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderedCategories();
    }
    
    public List<Category> getActiveCategories() {
        return categoryRepository.findAllActiveCategories();
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, Category categoryDetails) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            category.setDisplayOrder(categoryDetails.getDisplayOrder());
            category.setActive(categoryDetails.getActive());
            return categoryRepository.save(category);
        }
        return null;
    }
    
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }
    
    public List<String> getCategoryNames() {
        return getActiveCategories().stream()
                .map(Category::getName)
                .toList();
    }
}