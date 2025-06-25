import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Save, X, ChevronDown, ChevronRight } from 'lucide-react';

interface Filter {
  id?: number;
  name: string;
  displayName: string;
  description: string;
  displayOrder: number;
  active: boolean;
  filterValues?: FilterValue[];
}

interface FilterValue {
  id?: number;
  value: string;
  displayValue: string;
  displayOrder: number;
  active: boolean;
  filter?: Filter;
}

interface FilterManagerProps {
  token: string;
}

const FilterManager: React.FC<FilterManagerProps> = ({ token }) => {
  const [filters, setFilters] = useState<Filter[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingFilter, setEditingFilter] = useState<Filter | null>(null);
  const [editingFilterValue, setEditingFilterValue] = useState<FilterValue | null>(null);
  const [isCreatingFilter, setIsCreatingFilter] = useState(false);
  const [isCreatingFilterValue, setIsCreatingFilterValue] = useState<number | null>(null);
  const [expandedFilters, setExpandedFilters] = useState<Set<number>>(new Set());
  const [error, setError] = useState('');

  const emptyFilter: Filter = {
    name: '',
    displayName: '',
    description: '',
    displayOrder: 0,
    active: true
  };

  const emptyFilterValue: FilterValue = {
    value: '',
    displayValue: '',
    displayOrder: 0,
    active: true
  };

  useEffect(() => {
    fetchFilters();
  }, []);

  const fetchFilters = async () => {
    try {
      const response = await fetch('/api/admin/filters', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setFilters(data);
        
        // Fetch filter values for each filter
        for (const filter of data) {
          if (filter.id) {
            await fetchFilterValues(filter.id);
          }
        }
      } else {
        setError('Failed to fetch filters');
      }
    } catch (err) {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const fetchFilterValues = async (filterId: number) => {
    try {
      const response = await fetch(`/api/admin/filters/${filterId}/values`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const values = await response.json();
        setFilters(prev => prev.map(filter => 
          filter.id === filterId ? { ...filter, filterValues: values } : filter
        ));
      }
    } catch (err) {
      console.error('Failed to fetch filter values:', err);
    }
  };

  const handleSaveFilter = async (filter: Filter) => {
    try {
      const url = filter.id ? `/api/admin/filters/${filter.id}` : '/api/admin/filters';
      const method = filter.id ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(filter),
      });

      if (response.ok) {
        await fetchFilters();
        setEditingFilter(null);
        setIsCreatingFilter(false);
        setError('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to save filter');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const handleDeleteFilter = async (id: number) => {
    if (!confirm('Are you sure you want to delete this filter? This will also delete all its values.')) return;

    try {
      const response = await fetch(`/api/admin/filters/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        await fetchFilters();
        setError('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to delete filter');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const handleSaveFilterValue = async (filterValue: FilterValue, filterId: number) => {
    try {
      const url = filterValue.id 
        ? `/api/admin/filter-values/${filterValue.id}` 
        : `/api/admin/filters/${filterId}/values`;
      const method = filterValue.id ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(filterValue),
      });

      if (response.ok) {
        await fetchFilterValues(filterId);
        setEditingFilterValue(null);
        setIsCreatingFilterValue(null);
        setError('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to save filter value');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const handleDeleteFilterValue = async (id: number, filterId: number) => {
    if (!confirm('Are you sure you want to delete this filter value?')) return;

    try {
      const response = await fetch(`/api/admin/filter-values/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        await fetchFilterValues(filterId);
        setError('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to delete filter value');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const toggleFilterExpansion = (filterId: number) => {
    setExpandedFilters(prev => {
      const newSet = new Set(prev);
      if (newSet.has(filterId)) {
        newSet.delete(filterId);
      } else {
        newSet.add(filterId);
      }
      return newSet;
    });
  };

  const FilterForm: React.FC<{ filter: Filter; onSave: (filter: Filter) => void; onCancel: () => void }> = ({
    filter,
    onSave,
    onCancel
  }) => {
    const [formData, setFormData] = useState<Filter>(filter);

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      onSave(formData);
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h3 className="text-xl font-bold text-gray-900">
              {filter.id ? 'Edit Filter' : 'Add New Filter'}
            </h3>
            <button
              onClick={onCancel}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Filter Name (Internal)
              </label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., brand, color, size"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Display Name
              </label>
              <input
                type="text"
                required
                value={formData.displayName}
                onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., Brand, Color, Size"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Description
              </label>
              <textarea
                rows={3}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Optional description for this filter"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Display Order
              </label>
              <input
                type="number"
                min="0"
                value={formData.displayOrder}
                onChange={(e) => setFormData({ ...formData, displayOrder: parseInt(e.target.value) })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                value={formData.active.toString()}
                onChange={(e) => setFormData({ ...formData, active: e.target.value === 'true' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onCancel}
                className="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
              >
                <Save className="h-4 w-4" />
                <span>Save Filter</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const FilterValueForm: React.FC<{ 
    filterValue: FilterValue; 
    filterId: number;
    onSave: (filterValue: FilterValue, filterId: number) => void; 
    onCancel: () => void 
  }> = ({ filterValue, filterId, onSave, onCancel }) => {
    const [formData, setFormData] = useState<FilterValue>(filterValue);

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      onSave(formData, filterId);
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h3 className="text-xl font-bold text-gray-900">
              {filterValue.id ? 'Edit Filter Value' : 'Add New Filter Value'}
            </h3>
            <button
              onClick={onCancel}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Value (Internal)
              </label>
              <input
                type="text"
                required
                value={formData.value}
                onChange={(e) => setFormData({ ...formData, value: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., nike, red, large"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Display Value
              </label>
              <input
                type="text"
                required
                value={formData.displayValue}
                onChange={(e) => setFormData({ ...formData, displayValue: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., Nike, Red, Large"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Display Order
              </label>
              <input
                type="number"
                min="0"
                value={formData.displayOrder}
                onChange={(e) => setFormData({ ...formData, displayOrder: parseInt(e.target.value) })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                value={formData.active.toString()}
                onChange={(e) => setFormData({ ...formData, active: e.target.value === 'true' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onCancel}
                className="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
              >
                <Save className="h-4 w-4" />
                <span>Save Value</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Filter Management</h2>
        <button
          onClick={() => setIsCreatingFilter(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
        >
          <Plus className="h-4 w-4" />
          <span>Add Filter</span>
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        {filters.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
              <Plus className="h-8 w-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No filters yet</h3>
            <p className="text-gray-600 mb-4">Create your first filter to enable product filtering</p>
            <button
              onClick={() => setIsCreatingFilter(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Add First Filter
            </button>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {filters.map((filter) => (
              <div key={filter.id} className="p-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <button
                      onClick={() => filter.id && toggleFilterExpansion(filter.id)}
                      className="p-1 hover:bg-gray-100 rounded transition-colors"
                    >
                      {filter.id && expandedFilters.has(filter.id) ? (
                        <ChevronDown className="h-4 w-4 text-gray-500" />
                      ) : (
                        <ChevronRight className="h-4 w-4 text-gray-500" />
                      )}
                    </button>
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">{filter.displayName}</h3>
                      <p className="text-sm text-gray-500">
                        {filter.name} • {filter.filterValues?.length || 0} values
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                      filter.active 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {filter.active ? 'Active' : 'Inactive'}
                    </span>
                    
                    <button
                      onClick={() => setEditingFilter(filter)}
                      className="text-blue-600 hover:text-blue-900 p-1 hover:bg-blue-50 rounded"
                    >
                      <Edit className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => filter.id && handleDeleteFilter(filter.id)}
                      className="text-red-600 hover:text-red-900 p-1 hover:bg-red-50 rounded"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {filter.id && expandedFilters.has(filter.id) && (
                  <div className="mt-4 ml-7">
                    <div className="flex items-center justify-between mb-3">
                      <h4 className="text-sm font-medium text-gray-700">Filter Values</h4>
                      <button
                        onClick={() => setIsCreatingFilterValue(filter.id!)}
                        className="text-blue-600 hover:text-blue-700 text-sm flex items-center space-x-1"
                      >
                        <Plus className="h-3 w-3" />
                        <span>Add Value</span>
                      </button>
                    </div>
                    
                    <div className="space-y-2">
                      {filter.filterValues?.map((value) => (
                        <div key={value.id} className="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                          <div>
                            <span className="text-sm font-medium text-gray-900">{value.displayValue}</span>
                            <span className="text-xs text-gray-500 ml-2">({value.value})</span>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className={`px-2 py-1 text-xs rounded-full ${
                              value.active 
                                ? 'bg-green-100 text-green-700' 
                                : 'bg-red-100 text-red-700'
                            }`}>
                              {value.active ? 'Active' : 'Inactive'}
                            </span>
                            <button
                              onClick={() => setEditingFilterValue(value)}
                              className="text-blue-600 hover:text-blue-900 p-1"
                            >
                              <Edit className="h-3 w-3" />
                            </button>
                            <button
                              onClick={() => value.id && filter.id && handleDeleteFilterValue(value.id, filter.id)}
                              className="text-red-600 hover:text-red-900 p-1"
                            >
                              <Trash2 className="h-3 w-3" />
                            </button>
                          </div>
                        </div>
                      ))}
                      
                      {(!filter.filterValues || filter.filterValues.length === 0) && (
                        <p className="text-sm text-gray-500 italic">No values added yet</p>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Filter Form Modal */}
      {(editingFilter || isCreatingFilter) && (
        <FilterForm
          filter={editingFilter || emptyFilter}
          onSave={handleSaveFilter}
          onCancel={() => {
            setEditingFilter(null);
            setIsCreatingFilter(false);
            setError('');
          }}
        />
      )}

      {/* Filter Value Form Modal */}
      {(editingFilterValue || isCreatingFilterValue) && (
        <FilterValueForm
          filterValue={editingFilterValue || emptyFilterValue}
          filterId={isCreatingFilterValue || editingFilterValue?.filter?.id || 0}
          onSave={handleSaveFilterValue}
          onCancel={() => {
            setEditingFilterValue(null);
            setIsCreatingFilterValue(null);
            setError('');
          }}
        />
      )}
    </div>
  );
};

export default FilterManager;