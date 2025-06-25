package com.catalog.controller;

import com.catalog.dto.ProductDto;
import com.catalog.entity.Product;
import com.catalog.mapper.ProductMapper;
import com.catalog.service.CategoryService;
import com.catalog.service.ProductService;
import com.catalog.service.ProductFilterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductFilterService productFilterService;
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Map<String, String> filters) {
        
        List<Product> products;
        
        // Remove known parameters from filters map
        Map<String, String> actualFilters = filters.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("category") && 
                               !entry.getKey().equals("search") && 
                               !entry.getKey().equals("sort"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        // Apply search and category filters first
        if (category != null && !category.isEmpty() && search != null && !search.isEmpty()) {
            products = productService.searchProductsByCategory(category, search);
        } else if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        
        // Apply custom filters if any are provided
        if (!actualFilters.isEmpty()) {
            // Convert single values to lists for the filter service
            Map<String, List<String>> filterMap = actualFilters.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.of(entry.getValue().split(","))
                    ));
            
            List<Long> filteredProductIds = productFilterService.findProductIdsByFilters(filterMap);
            
            // Filter products to only include those that match the filters
            products = products.stream()
                    .filter(product -> filteredProductIds.contains(product.getId()))
                    .collect(Collectors.toList());
        }
        
        // Apply sorting
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc":
                    products = products.stream()
                            .sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                            .collect(Collectors.toList());
                    break;
                case "price_desc":
                    products = products.stream()
                            .sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
                            .collect(Collectors.toList());
                    break;
                case "latest":
                    products = products.stream()
                            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                            .collect(Collectors.toList());
                    break;
            }
        }
        
        List<ProductDto> productDtos = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(p -> ResponseEntity.ok(productMapper.toDto(p)))
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        ProductDto productDto = productMapper.toDto(savedProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        if (updatedProduct != null) {
            ProductDto productDto = productMapper.toDto(updatedProduct);
            return ResponseEntity.ok(productDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = categoryService.getCategoryNames();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductDto>> getInStockProducts() {
        List<Product> products = productService.getInStockProducts();
        List<ProductDto> productDtos = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDtos);
    }
}