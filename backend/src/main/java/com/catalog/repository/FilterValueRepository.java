package com.catalog.repository;

import com.catalog.entity.FilterValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterValueRepository extends JpaRepository<FilterValue, Long> {
    
    List<FilterValue> findByFilterIdAndActiveOrderByDisplayOrderAsc(Long filterId, Boolean active);
    
    List<FilterValue> findByFilterIdOrderByDisplayOrderAsc(Long filterId);
    
    @Query("SELECT fv FROM FilterValue fv WHERE fv.filter.id = :filterId AND fv.value = :value")
    Optional<FilterValue> findByFilterIdAndValue(@Param("filterId") Long filterId, @Param("value") String value);
    
    Boolean existsByFilterIdAndValue(Long filterId, String value);
}