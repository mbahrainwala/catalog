package com.catalog.mapper;

import com.catalog.dto.FilterDto;
import com.catalog.dto.FilterValueDto;
import com.catalog.entity.Filter;
import com.catalog.entity.FilterValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FilterMapper {
    
    public FilterDto toDto(Filter filter) {
        if (filter == null) {
            return null;
        }
        
        List<FilterValueDto> filterValueDtos = null;
        if (filter.getFilterValues() != null) {
            filterValueDtos = filter.getFilterValues().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        
        return new FilterDto(
            filter.getId(),
            filter.getName(),
            filter.getDisplayName(),
            filter.getDescription(),
            filter.getDisplayOrder(),
            filter.getActive(),
            filterValueDtos
        );
    }
    
    public FilterValueDto toDto(FilterValue filterValue) {
        if (filterValue == null) {
            return null;
        }
        
        return new FilterValueDto(
            filterValue.getId(),
            filterValue.getValue(),
            filterValue.getDisplayValue(),
            filterValue.getDisplayOrder(),
            filterValue.getActive()
        );
    }
    
    public List<FilterDto> toDtoList(List<Filter> filters) {
        if (filters == null) {
            return null;
        }
        
        return filters.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<FilterValueDto> toValueDtoList(List<FilterValue> filterValues) {
        if (filterValues == null) {
            return null;
        }
        
        return filterValues.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}