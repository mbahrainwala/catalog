package com.catalog.controller;

import com.catalog.dto.FilterDto;
import com.catalog.entity.Filter;
import com.catalog.mapper.FilterMapper;
import com.catalog.service.CategoryFilterService;
import com.catalog.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
@CrossOrigin(origins = "*")
public class FilterController {
    
    @Autowired
    private FilterService filterService;
    
    @Autowired
    private CategoryFilterService categoryFilterService;
    
    @Autowired
    private FilterMapper filterMapper;
    
    @GetMapping
    public ResponseEntity<List<FilterDto>> getActiveFiltersWithValues(@RequestParam(required = false) String category) {
        List<Filter> filters;
        
        if (category != null && !category.isEmpty() && !category.equals("all")) {
            // Get filters specific to the category
            filters = categoryFilterService.getActiveFiltersByCategoryName(category);
            
            // Load filter values for each filter and filter out those without active values
            filters = filters.stream()
                    .filter(filter -> {
                        List<com.catalog.entity.FilterValue> activeValues = filterService.getActiveFilterValues(filter.getId());
                        if (!activeValues.isEmpty()) {
                            filter.setFilterValues(activeValues);
                            return true;
                        }
                        return false;
                    })
                    .toList();
        } else {
            // Get all active filters with values
            filters = filterService.getActiveFiltersWithValues();
        }
        
        List<FilterDto> filterDtos = filterMapper.toDtoList(filters);
        return ResponseEntity.ok(filterDtos);
    }
}