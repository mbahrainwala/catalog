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

import java.util.*;
import java.util.stream.Collectors;

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
    
    public Map<String, List<String>> getProductFilterValues(Long productId) {
        List<ProductFilter> productFilters = productFilterRepository.findByProductId(productId);
        
        return productFilters.stream()
                .collect(Collectors.groupingBy(
                    pf -> pf.getFilter().getName(),
                    Collectors.mapping(
                        pf -> pf.getFilterValue().getValue(),
                        Collectors.toList()
                    )
                ));
    }
    
    public List<Long> findProductIdsByFilters(Map<String, List<String>> filters) {
        if (filters.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get all product IDs that match each filter
        List<Set<Long>> filterResults = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            String filterName = entry.getKey();
            List<String> values = entry.getValue();
            
            Optional<Filter> filterOpt = filterRepository.findByName(filterName);
            if (filterOpt.isPresent()) {
                List<Long> productIds = productFilterRepository.findProductIdsByFilterAndValues(
                    filterOpt.get().getId(), values);
                filterResults.add(new HashSet<>(productIds));
            }
        }
        
        // Find intersection of all filter results (products that match ALL filters)
        if (filterResults.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<Long> result = new HashSet<>(filterResults.get(0));
        for (int i = 1; i < filterResults.size(); i++) {
            result.retainAll(filterResults.get(i));
        }
        
        return new ArrayList<>(result);
    }
}