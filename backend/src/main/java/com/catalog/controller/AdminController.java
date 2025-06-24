package com.catalog.controller;

import com.catalog.entity.Category;
import com.catalog.entity.Product;
import com.catalog.service.CategoryService;
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
    
    // Product Management
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                               @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return updatedProduct != null ? ResponseEntity.ok(updatedProduct) 
                                     : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    // Category Management
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
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
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
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
        return updatedCategory != null ? ResponseEntity.ok(updatedCategory) 
                                      : ResponseEntity.notFound().build();
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
}