package com.catalog.service;

import com.catalog.entity.Product;
import com.catalog.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setCostPrice(productDetails.getCostPrice());
            product.setCategory(productDetails.getCategory());
            product.setInStock(productDetails.getInStock());
            return productRepository.save(product);
        }
        return null;
    }
    
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByKeyword(keyword);
    }
    
    public List<Product> searchProductsByCategory(String category, String keyword) {
        return productRepository.findByCategoryAndKeyword(category, keyword);
    }
    
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    
    public List<Product> getInStockProducts() {
        return productRepository.findByInStock(true);
    }
    
    public List<Product> getLatestProducts() {
        return productRepository.findByOrderByCreatedAtDesc();
    }
    
    public List<Product> getProductsSortedByPriceAsc() {
        return productRepository.findByOrderByPriceAsc();
    }
    
    public List<Product> getProductsSortedByPriceDesc() {
        return productRepository.findByOrderByPriceDesc();
    }
}