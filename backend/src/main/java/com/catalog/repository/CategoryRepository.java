package com.catalog.repository;

import com.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByActiveOrderByDisplayOrderAsc(Boolean active);
    
    Optional<Category> findByName(String name);
    
    Boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllActiveCategories();
    
    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllOrderedCategories();
}