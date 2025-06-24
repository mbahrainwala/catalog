package com.catalog.repository;

import com.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByInStock(Boolean inStock);
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.category = :category AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> findByCategoryAndKeyword(@Param("category") String category, 
                                         @Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();
    
    List<Product> findByOrderByCreatedAtDesc();
    
    List<Product> findByOrderByPriceAsc();
    
    List<Product> findByOrderByPriceDesc();
}