package com.catalog.repository;

import com.catalog.entity.CategoryFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryFilterRepository extends JpaRepository<CategoryFilter, Long> {
    
    List<CategoryFilter> findByCategoryId(Long categoryId);
    
    List<CategoryFilter> findByFilterId(Long filterId);
    
    void deleteByCategoryId(Long categoryId);
    
    void deleteByFilterId(Long filterId);
    
    @Query("SELECT cf.filter.id FROM CategoryFilter cf WHERE cf.category.name = :categoryName AND cf.filter.active = true")
    List<Long> findActiveFilterIdsByCategoryName(@Param("categoryName") String categoryName);
    
    @Query("SELECT DISTINCT cf.filter FROM CategoryFilter cf WHERE cf.category.name = :categoryName AND cf.filter.active = true ORDER BY cf.filter.displayOrder ASC, cf.filter.name ASC")
    List<com.catalog.entity.Filter> findActiveFiltersByCategoryName(@Param("categoryName") String categoryName);
    
    boolean existsByCategoryIdAndFilterId(Long categoryId, Long filterId);
}