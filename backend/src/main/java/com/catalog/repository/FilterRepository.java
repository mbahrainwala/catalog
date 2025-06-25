package com.catalog.repository;

import com.catalog.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Long> {
    
    List<Filter> findByActiveOrderByDisplayOrderAsc(Boolean active);
    
    Optional<Filter> findByName(String name);
    
    Boolean existsByName(String name);
    
    @Query("SELECT f FROM Filter f WHERE f.active = true ORDER BY f.displayOrder ASC, f.name ASC")
    List<Filter> findAllActiveFilters();
    
    @Query("SELECT f FROM Filter f ORDER BY f.displayOrder ASC, f.name ASC")
    List<Filter> findAllOrderedFilters();
}