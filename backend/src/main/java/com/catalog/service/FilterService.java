package com.catalog.service;

import com.catalog.entity.Filter;
import com.catalog.entity.FilterValue;
import com.catalog.repository.FilterRepository;
import com.catalog.repository.FilterValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FilterService {
    
    @Autowired
    private FilterRepository filterRepository;
    
    @Autowired
    private FilterValueRepository filterValueRepository;
    
    public List<Filter> getAllFilters() {
        return filterRepository.findAllOrderedFilters();
    }
    
    public List<Filter> getActiveFilters() {
        return filterRepository.findAllActiveFilters();
    }
    
    public List<Filter> getActiveFiltersWithValues() {
        List<Filter> filters = filterRepository.findAllActiveFilters();
        
        // Manually fetch and set active filter values for each filter
        for (Filter filter : filters) {
            List<FilterValue> activeValues = filterValueRepository.findByFilterIdAndActiveOrderByDisplayOrderAsc(filter.getId(), true);
            filter.setFilterValues(activeValues);
        }
        
        return filters;
    }
    
    public Optional<Filter> getFilterById(Long id) {
        return filterRepository.findById(id);
    }
    
    public Optional<Filter> getFilterByName(String name) {
        return filterRepository.findByName(name);
    }
    
    public Filter saveFilter(Filter filter) {
        return filterRepository.save(filter);
    }
    
    public Filter updateFilter(Long id, Filter filterDetails) {
        Optional<Filter> optionalFilter = filterRepository.findById(id);
        if (optionalFilter.isPresent()) {
            Filter filter = optionalFilter.get();
            filter.setName(filterDetails.getName());
            filter.setDisplayName(filterDetails.getDisplayName());
            filter.setDescription(filterDetails.getDescription());
            filter.setDisplayOrder(filterDetails.getDisplayOrder());
            filter.setActive(filterDetails.getActive());
            return filterRepository.save(filter);
        }
        return null;
    }
    
    @Transactional
    public boolean deleteFilter(Long id) {
        if (filterRepository.existsById(id)) {
            // Delete all filter values first
            List<FilterValue> filterValues = filterValueRepository.findByFilterIdOrderByDisplayOrderAsc(id);
            filterValueRepository.deleteAll(filterValues);
            
            // Then delete the filter
            filterRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean filterExists(String name) {
        return filterRepository.existsByName(name);
    }
    
    // Filter Value methods
    public List<FilterValue> getFilterValues(Long filterId) {
        return filterValueRepository.findByFilterIdOrderByDisplayOrderAsc(filterId);
    }
    
    public List<FilterValue> getActiveFilterValues(Long filterId) {
        return filterValueRepository.findByFilterIdAndActiveOrderByDisplayOrderAsc(filterId, true);
    }
    
    public Optional<FilterValue> getFilterValueById(Long id) {
        return filterValueRepository.findById(id);
    }
    
    public FilterValue saveFilterValue(FilterValue filterValue) {
        return filterValueRepository.save(filterValue);
    }
    
    public FilterValue updateFilterValue(Long id, FilterValue filterValueDetails) {
        Optional<FilterValue> optionalFilterValue = filterValueRepository.findById(id);
        if (optionalFilterValue.isPresent()) {
            FilterValue filterValue = optionalFilterValue.get();
            filterValue.setValue(filterValueDetails.getValue());
            filterValue.setDisplayValue(filterValueDetails.getDisplayValue());
            filterValue.setDisplayOrder(filterValueDetails.getDisplayOrder());
            filterValue.setActive(filterValueDetails.getActive());
            return filterValueRepository.save(filterValue);
        }
        return null;
    }
    
    public boolean deleteFilterValue(Long id) {
        if (filterValueRepository.existsById(id)) {
            filterValueRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean filterValueExists(Long filterId, String value) {
        return filterValueRepository.existsByFilterIdAndValue(filterId, value);
    }
}