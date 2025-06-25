package com.catalog.controller;

import com.catalog.dto.ProductDto;
import com.catalog.entity.Product;
import com.catalog.mapper.ProductMapper;
import com.catalog.service.CategoryService;
import com.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        
        List<Product> products;
        
        // Apply search and category filters
        if (category != null && !category.isEmpty() && search != null && !search.isEmpty()) {
            products = productService.searchProductsByCategory(category, search);
        } else if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        
        // Apply sorting
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc":
                    products = productService.getProductsSortedByPriceAsc();
                    break;
                case "price_desc":
                    products = productService.getProductsSortedByPriceDesc();
                    break;
                case "latest":
                    products = productService.getLatestProducts();
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