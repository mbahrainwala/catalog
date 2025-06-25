package com.catalog.controller;

import com.catalog.dto.FilterDto;
import com.catalog.entity.Filter;
import com.catalog.mapper.FilterMapper;
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
    private FilterMapper filterMapper;
    
    @GetMapping
    public ResponseEntity<List<FilterDto>> getActiveFiltersWithValues() {
        List<Filter> filters = filterService.getActiveFiltersWithValues();
        List<FilterDto> filterDtos = filterMapper.toDtoList(filters);
        return ResponseEntity.ok(filterDtos);
    }
}