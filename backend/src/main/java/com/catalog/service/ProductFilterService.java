package com.catalog.service;

import com.catalog.entity.Product;
import com.catalog.entity.ProductFilter;
import com.catalog.entity.Filter;
import com.catalog.entity.FilterValue;
import com.catalog.repository.ProductFilterRepository;
import com.catalog.repository.FilterRepository;
import com.catalog.repository.FilterValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductFilterService {
    
    @Autowired
    private ProductFilterRepository productFilterRepository;
    
    @Autowired
    private FilterRepository filterRepository;
    
    @Autowired
    private FilterValueRepository filterValueRepository;
    
    public List<ProductFilter> getProductFilters(Long productId) {
        return productFilterRepository.findByProductId(productId);
    }
    
    @Transactional
    public void updateProductFilters(Product product, Map<String, List<String>> filterData) {
        // Remove existing filters for this product
        productFilterRepository.deleteByProductId(product.getId());
        
        // Add new filters
        for (Map.Entry<String, List<String>> entry : filterData.entrySet()) {
            String filterName = entry.getKey();
            List<String> values = entry.getValue();
            
            Optional<Filter> filterOpt = filterRepository.findByName(filterName);
            if (filterOpt.isPresent()) {
                Filter filter = filterOpt.get();
                
                for (String value : values) {
                    Optional<FilterValue> filterValueOpt = filterValueRepository.findByFilterIdAndValue(filter.getId(), value);
                    if (filterValueOpt.isPresent()) {
                        ProductFilter productFilter = new ProductFilter(product, filter, filterValueOpt.get());
                        productFilterRepository.save(productFilter);
                    }
                }
            }
        }
    }
    
    public List<Long> findProductIdsByFilters(Map<String, List<String>> filters) {
        // This is a simplified implementation - in a real scenario, you'd want to implement
        // proper intersection logic for multiple filters
        if (filters.isEmpty()) {
            return List.of();
        }
        
        // For now, we'll just handle one filter at a time
        Map.Entry<String, List<String>> firstFilter = filters.entrySet().iterator().next();
        String filterName = firstFilter.getKey();
        List<String> values = firstFilter.getValue();
        
        Optional<Filter> filterOpt = filterRepository.findByName(filterName);
        if (filterOpt.isPresent()) {
            return productFilterRepository.findProductIdsByFilterAndValues(filterOpt.get().getId(), values);
        }
        
        return List.of();
    }
}