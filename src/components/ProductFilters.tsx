import React, { useState, useEffect } from 'react';
import { X, ChevronDown, ChevronUp } from 'lucide-react';

interface Filter {
  id: number;
  name: string;
  displayName: string;
  description: string;
  displayOrder: number;
  active: boolean;
  filterValues: FilterValue[];
}

interface FilterValue {
  id: number;
  value: string;
  displayValue: string;
  displayOrder: number;
  active: boolean;
}

interface ProductFiltersProps {
  onFiltersChange: (filters: Record<string, string[]>) => void;
}

const ProductFilters: React.FC<ProductFiltersProps> = ({ onFiltersChange }) => {
  const [filters, setFilters] = useState<Filter[]>([]);
  const [selectedFilters, setSelectedFilters] = useState<Record<string, string[]>>({});
  const [expandedFilters, setExpandedFilters] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFilters();
  }, []);

  useEffect(() => {
    onFiltersChange(selectedFilters);
  }, [selectedFilters, onFiltersChange]);

  const fetchFilters = async () => {
    try {
      const response = await fetch('/api/filters');
      if (response.ok) {
        const data = await response.json();
        setFilters(data);
        
        // Auto-expand all filters initially
        const filterNames = data.map((filter: Filter) => filter.name);
        setExpandedFilters(new Set(filterNames));
      }
    } catch (err) {
      console.error('Failed to fetch filters:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterValueChange = (filterName: string, value: string, checked: boolean) => {
    setSelectedFilters(prev => {
      const newFilters = { ...prev };
      
      if (!newFilters[filterName]) {
        newFilters[filterName] = [];
      }
      
      if (checked) {
        if (!newFilters[filterName].includes(value)) {
          newFilters[filterName] = [...newFilters[filterName], value];
        }
      } else {
        newFilters[filterName] = newFilters[filterName].filter(v => v !== value);
        if (newFilters[filterName].length === 0) {
          delete newFilters[filterName];
        }
      }
      
      return newFilters;
    });
  };

  const toggleFilterExpansion = (filterName: string) => {
    setExpandedFilters(prev => {
      const newSet = new Set(prev);
      if (newSet.has(filterName)) {
        newSet.delete(filterName);
      } else {
        newSet.add(filterName);
      }
      return newSet;
    });
  };

  const clearAllFilters = () => {
    setSelectedFilters({});
  };

  const clearFilter = (filterName: string) => {
    setSelectedFilters(prev => {
      const newFilters = { ...prev };
      delete newFilters[filterName];
      return newFilters;
    });
  };

  const getSelectedCount = () => {
    return Object.values(selectedFilters).reduce((total, values) => total + values.length, 0);
  };

  if (loading) {
    return (
      <div className="bg-white rounded-xl shadow-sm p-4">
        <div className="animate-pulse space-y-4">
          <div className="h-4 bg-gray-200 rounded w-1/2"></div>
          <div className="space-y-2">
            <div className="h-3 bg-gray-200 rounded"></div>
            <div className="h-3 bg-gray-200 rounded w-3/4"></div>
            <div className="h-3 bg-gray-200 rounded w-1/2"></div>
          </div>
        </div>
      </div>
    );
  }

  if (filters.length === 0) {
    return null;
  }

  return (
    <div className="bg-white rounded-xl shadow-sm">
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
          {getSelectedCount() > 0 && (
            <button
              onClick={clearAllFilters}
              className="text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              Clear All ({getSelectedCount()})
            </button>
          )}
        </div>
      </div>

      <div className="p-4 space-y-4">
        {/* Active Filters */}
        {Object.keys(selectedFilters).length > 0 && (
          <div className="pb-4 border-b border-gray-200">
            <h4 className="text-sm font-medium text-gray-700 mb-2">Active Filters:</h4>
            <div className="flex flex-wrap gap-2">
              {Object.entries(selectedFilters).map(([filterName, values]) => {
                const filter = filters.find(f => f.name === filterName);
                return values.map(value => {
                  const filterValue = filter?.filterValues.find(fv => fv.value === value);
                  return (
                    <span
                      key={`${filterName}-${value}`}
                      className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                    >
                      {filter?.displayName}: {filterValue?.displayValue || value}
                      <button
                        onClick={() => handleFilterValueChange(filterName, value, false)}
                        className="ml-1 hover:bg-blue-200 rounded-full p-0.5"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  );
                });
              })}
            </div>
          </div>
        )}

        {/* Filter Groups */}
        {filters.map((filter) => (
          <div key={filter.id} className="border-b border-gray-100 last:border-b-0 pb-4 last:pb-0">
            <button
              onClick={() => toggleFilterExpansion(filter.name)}
              className="flex items-center justify-between w-full text-left"
            >
              <div className="flex items-center space-x-2">
                <h4 className="text-sm font-medium text-gray-900">{filter.displayName}</h4>
                {selectedFilters[filter.name] && (
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">
                    {selectedFilters[filter.name].length}
                  </span>
                )}
              </div>
              {expandedFilters.has(filter.name) ? (
                <ChevronUp className="h-4 w-4 text-gray-500" />
              ) : (
                <ChevronDown className="h-4 w-4 text-gray-500" />
              )}
            </button>

            {expandedFilters.has(filter.name) && (
              <div className="mt-3 space-y-2">
                {filter.filterValues
                  .filter(fv => fv.active)
                  .sort((a, b) => a.displayOrder - b.displayOrder)
                  .map((filterValue) => (
                    <label
                      key={filterValue.id}
                      className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                    >
                      <input
                        type="checkbox"
                        checked={selectedFilters[filter.name]?.includes(filterValue.value) || false}
                        onChange={(e) => handleFilterValueChange(filter.name, filterValue.value, e.target.checked)}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                      <span className="text-sm text-gray-700">{filterValue.displayValue}</span>
                    </label>
                  ))}
                
                {filter.filterValues.filter(fv => fv.active).length === 0 && (
                  <p className="text-sm text-gray-500 italic">No options available</p>
                )}
                
                {selectedFilters[filter.name] && selectedFilters[filter.name].length > 0 && (
                  <button
                    onClick={() => clearFilter(filter.name)}
                    className="text-xs text-blue-600 hover:text-blue-700 mt-2"
                  >
                    Clear {filter.displayName}
                  </button>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductFilters;