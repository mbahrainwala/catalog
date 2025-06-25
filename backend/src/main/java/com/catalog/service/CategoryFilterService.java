package com.catalog.service;

import com.catalog.entity.Category;
import com.catalog.entity.CategoryFilter;
import com.catalog.entity.Filter;
import com.catalog.repository.CategoryFilterRepository;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.FilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryFilterService {
    
    @Autowired
    private CategoryFilterRepository categoryFilterRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FilterRepository filterRepository;
    
    public List<CategoryFilter> getCategoryFilters(Long categoryId) {
        return categoryFilterRepository.findByCategoryId(categoryId);
    }
    
    public List<Filter> getActiveFiltersByCategoryName(String categoryName) {
        return categoryFilterRepository.findActiveFiltersByCategoryName(categoryName);
    }
    
    @Transactional
    public void updateCategoryFilters(Long categoryId, List<Long> filterIds) {
        // Remove existing category-filter relationships
        categoryFilterRepository.deleteByCategoryId(categoryId);
        
        // Add new relationships
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            
            for (Long filterId : filterIds) {
                Optional<Filter> filterOpt = filterRepository.findById(filterId);
                if (filterOpt.isPresent()) {
                    CategoryFilter categoryFilter = new CategoryFilter(category, filterOpt.get());
                    categoryFilterRepository.save(categoryFilter);
                }
            }
        }
    }
    
    public boolean addFilterToCategory(Long categoryId, Long filterId) {
        if (categoryFilterRepository.existsByCategoryIdAndFilterId(categoryId, filterId)) {
            return false; // Already exists
        }
        
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        Optional<Filter> filterOpt = filterRepository.findById(filterId);
        
        if (categoryOpt.isPresent() && filterOpt.isPresent()) {
            CategoryFilter categoryFilter = new CategoryFilter(categoryOpt.get(), filterOpt.get());
            categoryFilterRepository.save(categoryFilter);
            return true;
        }
        
        return false;
    }
    
    public boolean removeFilterFromCategory(Long categoryId, Long filterId) {
        List<CategoryFilter> categoryFilters = categoryFilterRepository.findByCategoryId(categoryId);
        
        for (CategoryFilter cf : categoryFilters) {
            if (cf.getFilter().getId().equals(filterId)) {
                categoryFilterRepository.delete(cf);
                return true;
            }
        }
        
        return false;
    }
}