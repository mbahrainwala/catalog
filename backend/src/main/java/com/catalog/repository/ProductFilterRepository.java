package com.catalog.repository;

import com.catalog.entity.ProductFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFilterRepository extends JpaRepository<ProductFilter, Long> {
    
    List<ProductFilter> findByProductId(Long productId);
    
    List<ProductFilter> findByFilterId(Long filterId);
    
    List<ProductFilter> findByFilterValueId(Long filterValueId);
    
    void deleteByProductId(Long productId);
    
    @Query("SELECT DISTINCT pf.product.id FROM ProductFilter pf WHERE pf.filter.id = :filterId AND pf.filterValue.value IN :values")
    List<Long> findProductIdsByFilterAndValues(@Param("filterId") Long filterId, @Param("values") List<String> values);
    
    @Query("SELECT pf FROM ProductFilter pf WHERE pf.product.id = :productId AND pf.filter.id = :filterId")
    List<ProductFilter> findByProductIdAndFilterId(@Param("productId") Long productId, @Param("filterId") Long filterId);
}