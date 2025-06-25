package com.catalog.controller;

import com.catalog.dto.CategoryDto;
import com.catalog.dto.FilterDto;
import com.catalog.dto.FilterValueDto;
import com.catalog.dto.ProductDto;
import com.catalog.entity.Category;
import com.catalog.entity.Filter;
import com.catalog.entity.FilterValue;
import com.catalog.entity.Product;
import com.catalog.mapper.CategoryMapper;
import com.catalog.mapper.FilterMapper;
import com.catalog.mapper.ProductMapper;
import com.catalog.service.CategoryFilterService;
import com.catalog.service.CategoryService;
import com.catalog.service.FilterService;
import com.catalog.service.ProductFilterService;
import com.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private FilterService filterService;
    
    @Autowired
    private CategoryFilterService categoryFilterService;
    
    @Autowired
    private ProductFilterService productFilterService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private FilterMapper filterMapper;
    
    // Product Management - Return DTOs
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDtos);
    }
    
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(p -> ResponseEntity.ok(productMapper.toDto(p)))
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody Map<String, Object> productData) {
        try {
            // Extract product data
            Product product = new Product();
            product.setName((String) productData.get("name"));
            product.setDescription((String) productData.get("description"));
            product.setPrice(new java.math.BigDecimal(productData.get("price").toString()));
            product.setCategory((String) productData.get("category"));
            product.setImageUrl((String) productData.get("imageUrl"));
            
            if (productData.get("rating") != null) {
                product.setRating(new java.math.BigDecimal(productData.get("rating").toString()));
            }
            
            product.setInStock((Boolean) productData.get("inStock"));
            
            // Save product first
            Product savedProduct = productService.saveProduct(product);
            
            // Handle filter values if provided
            @SuppressWarnings("unchecked")
            Map<String, List<String>> filterData = (Map<String, List<String>>) productData.get("filterValues");
            if (filterData != null && !filterData.isEmpty()) {
                productFilterService.updateProductFilters(savedProduct, filterData);
            }
            
            ProductDto productDto = productMapper.toDto(savedProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
        } catch (Exception e) {
            e.printStackTrace(); // Add logging to see the actual error
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create product: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody Map<String, Object> productData) {
        try {
            Optional<Product> existingProductOpt = productService.getProductById(id);
            if (!existingProductOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = existingProductOpt.get();
            product.setName((String) productData.get("name"));
            product.setDescription((String) productData.get("description"));
            product.setPrice(new java.math.BigDecimal(productData.get("price").toString()));
            product.setCategory((String) productData.get("category"));
            product.setImageUrl((String) productData.get("imageUrl"));
            
            if (productData.get("rating") != null) {
                product.setRating(new java.math.BigDecimal(productData.get("rating").toString()));
            }
            
            product.setInStock((Boolean) productData.get("inStock"));
            
            // Update product
            Product updatedProduct = productService.updateProduct(id, product);
            
            // Handle filter values if provided
            @SuppressWarnings("unchecked")
            Map<String, List<String>> filterData = (Map<String, List<String>>) productData.get("filterValues");
            if (filterData != null) {
                productFilterService.updateProductFilters(updatedProduct, filterData);
            }
            
            ProductDto productDto = productMapper.toDto(updatedProduct);
            return ResponseEntity.ok(productDto);
        } catch (Exception e) {
            e.printStackTrace(); // Add logging to see the actual error
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    // Category Management - Return DTOs
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        return ResponseEntity.ok(categoryDtos);
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(c -> ResponseEntity.ok(categoryMapper.toDto(c)))
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody Category category) {
        Map<String, String> response = new HashMap<>();
        
        if (categoryService.categoryExists(category.getName())) {
            response.put("message", "Category with this name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        Category savedCategory = categoryService.saveCategory(category);
        CategoryDto categoryDto = categoryMapper.toDto(savedCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDto);
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, 
                                          @Valid @RequestBody Category categoryDetails) {
        Map<String, String> response = new HashMap<>();
        
        // Check if another category with the same name exists (excluding current one)
        Optional<Category> existingCategory = categoryService.getCategoryByName(categoryDetails.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            response.put("message", "Category with this name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
        if (updatedCategory != null) {
            CategoryDto categoryDto = categoryMapper.toDto(updatedCategory);
            return ResponseEntity.ok(categoryDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        
        // Check if category is being used by any products
        List<Product> productsUsingCategory = productService.getAllProducts().stream()
                .filter(product -> {
                    Optional<Category> category = categoryService.getCategoryById(id);
                    return category.isPresent() && product.getCategory().equals(category.get().getName());
                })
                .toList();
        
        if (!productsUsingCategory.isEmpty()) {
            response.put("message", "Cannot delete category. It is being used by " + productsUsingCategory.size() + " product(s)");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean deleted = categoryService.deleteCategory(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    // Category-Filter Management
    @GetMapping("/categories/{categoryId}/filters")
    public ResponseEntity<List<FilterDto>> getCategoryFilters(@PathVariable Long categoryId) {
        List<com.catalog.entity.CategoryFilter> categoryFilters = categoryFilterService.getCategoryFilters(categoryId);
        List<Filter> filters = categoryFilters.stream()
                .map(cf -> cf.getFilter())
                .toList();
        List<FilterDto> filterDtos = filterMapper.toDtoList(filters);
        return ResponseEntity.ok(filterDtos);
    }
    
    @PostMapping("/categories/{categoryId}/filters")
    public ResponseEntity<?> updateCategoryFilters(@PathVariable Long categoryId, 
                                                  @RequestBody List<Long> filterIds) {
        try {
            categoryFilterService.updateCategoryFilters(categoryId, filterIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update category filters");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Filter Management - Return DTOs instead of entities
    @GetMapping("/filters")
    public ResponseEntity<List<FilterDto>> getAllFilters() {
        List<Filter> filters = filterService.getAllFilters();
        List<FilterDto> filterDtos = filterMapper.toDtoList(filters);
        return ResponseEntity.ok(filterDtos);
    }
    
    @GetMapping("/filters/{id}")
    public ResponseEntity<FilterDto> getFilterById(@PathVariable Long id) {
        Optional<Filter> filter = filterService.getFilterById(id);
        return filter.map(f -> ResponseEntity.ok(filterMapper.toDto(f)))
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/filters")
    public ResponseEntity<?> createFilter(@Valid @RequestBody Filter filter) {
        Map<String, String> response = new HashMap<>();
        
        if (filterService.filterExists(filter.getName())) {
            response.put("message", "Filter with this name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        Filter savedFilter = filterService.saveFilter(filter);
        FilterDto filterDto = filterMapper.toDto(savedFilter);
        return ResponseEntity.status(HttpStatus.CREATED).body(filterDto);
    }
    
    @PutMapping("/filters/{id}")
    public ResponseEntity<?> updateFilter(@PathVariable Long id, 
                                        @Valid @RequestBody Filter filterDetails) {
        Map<String, String> response = new HashMap<>();
        
        // Check if another filter with the same name exists (excluding current one)
        Optional<Filter> existingFilter = filterService.getFilterByName(filterDetails.getName());
        if (existingFilter.isPresent() && !existingFilter.get().getId().equals(id)) {
            response.put("message", "Filter with this name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        Filter updatedFilter = filterService.updateFilter(id, filterDetails);
        if (updatedFilter != null) {
            FilterDto filterDto = filterMapper.toDto(updatedFilter);
            return ResponseEntity.ok(filterDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/filters/{id}")
    public ResponseEntity<?> deleteFilter(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        
        boolean deleted = filterService.deleteFilter(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    // Filter Value Management - Return DTOs instead of entities
    @GetMapping("/filters/{filterId}/values")
    public ResponseEntity<List<FilterValueDto>> getFilterValues(@PathVariable Long filterId) {
        List<FilterValue> filterValues = filterService.getFilterValues(filterId);
        List<FilterValueDto> filterValueDtos = filterMapper.toValueDtoList(filterValues);
        return ResponseEntity.ok(filterValueDtos);
    }
    
    @PostMapping("/filters/{filterId}/values")
    public ResponseEntity<?> createFilterValue(@PathVariable Long filterId, 
                                             @Valid @RequestBody FilterValue filterValue) {
        Map<String, String> response = new HashMap<>();
        
        Optional<Filter> filterOpt = filterService.getFilterById(filterId);
        if (!filterOpt.isPresent()) {
            response.put("message", "Filter not found");
            return ResponseEntity.notFound().build();
        }
        
        if (filterService.filterValueExists(filterId, filterValue.getValue())) {
            response.put("message", "Filter value already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        filterValue.setFilter(filterOpt.get());
        FilterValue savedFilterValue = filterService.saveFilterValue(filterValue);
        FilterValueDto filterValueDto = filterMapper.toDto(savedFilterValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(filterValueDto);
    }
    
    @PutMapping("/filter-values/{id}")
    public ResponseEntity<?> updateFilterValue(@PathVariable Long id, 
                                             @Valid @RequestBody FilterValue filterValueDetails) {
        Map<String, String> response = new HashMap<>();
        
        Optional<FilterValue> existingFilterValue = filterService.getFilterValueById(id);
        if (!existingFilterValue.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Long filterId = existingFilterValue.get().getFilter().getId();
        
        // Check if another filter value with the same value exists (excluding current one)
        if (filterService.filterValueExists(filterId, filterValueDetails.getValue())) {
            Optional<FilterValue> duplicateValue = filterService.getFilterValues(filterId).stream()
                    .filter(fv -> fv.getValue().equals(filterValueDetails.getValue()) && !fv.getId().equals(id))
                    .findFirst();
            
            if (duplicateValue.isPresent()) {
                response.put("message", "Filter value already exists");
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        FilterValue updatedFilterValue = filterService.updateFilterValue(id, filterValueDetails);
        if (updatedFilterValue != null) {
            FilterValueDto filterValueDto = filterMapper.toDto(updatedFilterValue);
            return ResponseEntity.ok(filterValueDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/filter-values/{id}")
    public ResponseEntity<?> deleteFilterValue(@PathVariable Long id) {
        boolean deleted = filterService.deleteFilterValue(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
}