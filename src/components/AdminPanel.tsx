import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Save, X, Package, Tags, Filter as FilterIcon } from 'lucide-react';
import CategoryManager from './CategoryManager';
import FilterManager from './FilterManager';
import ImageUpload from './ImageUpload';

interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  rating: number;
  inStock: boolean;
  filterValues?: Record<string, string[]>;
  images?: ProductImage[];
}

interface ProductImage {
  id: number;
  imageUrl: string;
  altText: string;
  displayOrder: number;
  isPrimary: boolean;
  originalFilename: string;
}

interface Category {
  id: number;
  name: string;
  description: string;
  displayOrder: number;
  active: boolean;
}

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

interface AdminPanelProps {
  token: string;
}

const AdminPanel: React.FC<AdminPanelProps> = ({ token }) => {
  const [activeTab, setActiveTab] = useState<'products' | 'categories' | 'filters'>('products');
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [availableFilters, setAvailableFilters] = useState<Filter[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState('');

  const emptyProduct: Product = {
    name: '',
    description: '',
    price: 0,
    category: '',
    imageUrl: '',
    rating: 0,
    inStock: true,
    filterValues: {}
  };

  useEffect(() => {
    fetchProducts();
    fetchCategories();
    fetchFilters();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch('/api/admin/products', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        console.log('Fetched products:', data); // Debug log
        setProducts(data);
      } else {
        setError('Failed to fetch products');
      }
    } catch (err) {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await fetch('/api/admin/categories', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setCategories(data);
      }
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const fetchFilters = async () => {
    try {
      const response = await fetch('/api/admin/filters', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setAvailableFilters(data);
      }
    } catch (err) {
      console.error('Failed to fetch filters:', err);
    }
  };

  const fetchCategoryFilters = async (categoryName: string): Promise<Filter[]> => {
    try {
      const response = await fetch(`/api/filters?category=${encodeURIComponent(categoryName)}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        return data;
      }
    } catch (err) {
      console.error('Failed to fetch category filters:', err);
    }
    return [];
  };

  const handleSave = async (product: Product) => {
    try {
      console.log('Saving product with data:', product); // Debug log
      
      const url = product.id ? `/api/admin/products/${product.id}` : '/api/admin/products';
      const method = product.id ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(product),
      });

      if (response.ok) {
        await fetchProducts(); // Refresh the products list
        setEditingProduct(null);
        setIsCreating(false);
        setError('');
      } else {
        const errorData = await response.text();
        console.error('Save error response:', errorData); // Debug log
        setError('Failed to save product: ' + errorData);
      }
    } catch (err) {
      console.error('Save error:', err); // Debug log
      setError('Network error');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
      const response = await fetch(`/api/admin/products/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        await fetchProducts();
        setError('');
      } else {
        setError('Failed to delete product');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const ProductForm: React.FC<{ product: Product; onSave: (product: Product) => void; onCancel: () => void }> = ({
    product,
    onSave,
    onCancel
  }) => {
    const [formData, setFormData] = useState<Product>(product);
    const [categoryFilters, setCategoryFilters] = useState<Filter[]>([]);
    const [selectedFilterValues, setSelectedFilterValues] = useState<Record<string, string[]>>(
      product.filterValues || {}
    );

    useEffect(() => {
      if (formData.category) {
        fetchCategoryFilters(formData.category).then(setCategoryFilters);
      } else {
        setCategoryFilters([]);
      }
    }, [formData.category]);

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      console.log('Form submission - filterValues:', selectedFilterValues); // Debug log
      onSave({ ...formData, filterValues: selectedFilterValues });
    };

    const handleCategoryChange = (categoryName: string) => {
      setFormData({ ...formData, category: categoryName });
      setSelectedFilterValues({}); // Clear filter values when category changes
    };

    const handleFilterValueChange = (filterName: string, value: string, checked: boolean) => {
      console.log('Filter value change:', filterName, value, checked); // Debug log
      
      setSelectedFilterValues(prev => {
        const newValues = { ...prev };
        
        if (!newValues[filterName]) {
          newValues[filterName] = [];
        }
        
        if (checked) {
          if (!newValues[filterName].includes(value)) {
            newValues[filterName] = [...newValues[filterName], value];
          }
        } else {
          newValues[filterName] = newValues[filterName].filter(v => v !== value);
          if (newValues[filterName].length === 0) {
            delete newValues[filterName];
          }
        }
        
        console.log('Updated filter values:', newValues); // Debug log
        return newValues;
      });
    };

    const handleImagesChange = () => {
      // Refresh the products list to get updated image data
      fetchProducts();
    };

    const activeCategoryNames = categories
      .filter(cat => cat.active)
      .sort((a, b) => a.displayOrder - b.displayOrder)
      .map(cat => cat.name);

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-y-auto">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h3 className="text-xl font-bold text-gray-900">
              {product.id ? 'Edit Product' : 'Add New Product'}
            </h3>
            <button
              onClick={onCancel}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Basic Product Information */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900">Basic Information</h4>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Product Name
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Description
                  </label>
                  <textarea
                    required
                    rows={3}
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Category
                  </label>
                  <select
                    required
                    value={formData.category}
                    onChange={(e) => handleCategoryChange(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">Select Category</option>
                    {activeCategoryNames.map(categoryName => (
                      <option key={categoryName} value={categoryName}>
                        {categoryName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Price ($)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      required
                      value={formData.price}
                      onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Rating (0-5)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      min="0"
                      max="5"
                      value={formData.rating}
                      onChange={(e) => setFormData({ ...formData, rating: parseFloat(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Legacy Image URL (Optional)
                  </label>
                  <input
                    type="url"
                    value={formData.imageUrl}
                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="https://example.com/image.jpg"
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Use image upload below instead of URL when possible
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stock Status
                  </label>
                  <select
                    value={formData.inStock.toString()}
                    onChange={(e) => setFormData({ ...formData, inStock: e.target.value === 'true' })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="true">In Stock</option>
                    <option value="false">Out of Stock</option>
                  </select>
                </div>

                {/* Filter Values */}
                <div>
                  <h5 className="text-md font-medium text-gray-900 mb-3">Filter Values</h5>
                  
                  {formData.category ? (
                    categoryFilters.length > 0 ? (
                      <div className="space-y-4 max-h-64 overflow-y-auto">
                        {categoryFilters.map(filter => (
                          <div key={filter.id} className="border border-gray-200 rounded-lg p-3">
                            <h6 className="font-medium text-gray-900 mb-2">{filter.displayName}</h6>
                            <div className="space-y-1">
                              {filter.filterValues
                                .filter(fv => fv.active)
                                .sort((a, b) => a.displayOrder - b.displayOrder)
                                .map(filterValue => (
                                  <label
                                    key={filterValue.id}
                                    className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                                  >
                                    <input
                                      type="checkbox"
                                      checked={selectedFilterValues[filter.name]?.includes(filterValue.value) || false}
                                      onChange={(e) => handleFilterValueChange(filter.name, filterValue.value, e.target.checked)}
                                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-sm text-gray-700">{filterValue.displayValue}</span>
                                  </label>
                                ))}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="text-center py-4 text-gray-500 text-sm">
                        No filters available for this category.
                      </div>
                    )
                  ) : (
                    <div className="text-center py-4 text-gray-500 text-sm">
                      Please select a category first to see available filters.
                    </div>
                  )}
                </div>
              </div>

              {/* Image Upload */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900">Product Images</h4>
                
                {product.id ? (
                  <ImageUpload
                    productId={product.id}
                    images={product.images || []}
                    token={token}
                    onImagesChange={handleImagesChange}
                  />
                ) : (
                  <div className="text-center py-8 text-gray-500 border-2 border-dashed border-gray-300 rounded-lg">
                    <p>Save the product first to upload images</p>
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
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
                <span>Save Product</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const getProductDisplayImage = (product: Product) => {
    if (product.images && product.images.length > 0) {
      const primaryImage = product.images.find(img => img.isPrimary);
      return primaryImage ? primaryImage.imageUrl : product.images[0].imageUrl;
    }
    return product.imageUrl;
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
      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('products')}
            className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'products'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Package className="h-4 w-4 inline mr-2" />
            Products
          </button>
          <button
            onClick={() => setActiveTab('categories')}
            className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'categories'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Tags className="h-4 w-4 inline mr-2" />
            Categories
          </button>
          <button
            onClick={() => setActiveTab('filters')}
            className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'filters'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <FilterIcon className="h-4 w-4 inline mr-2" />
            Filters
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'categories' ? (
        <CategoryManager token={token} />
      ) : activeTab === 'filters' ? (
        <FilterManager token={token} />
      ) : (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900">Product Management</h2>
            <button
              onClick={() => setIsCreating(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
            >
              <Plus className="h-4 w-4" />
              <span>Add Product</span>
            </button>
          </div>

          {error && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
              {error}
            </div>
          )}

          <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Category
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Price
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Images
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Filters
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {products.map((product) => (
                    <tr key={product.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <img
                            className="h-10 w-10 rounded-lg object-cover"
                            src={getProductDisplayImage(product)}
                            alt={product.name}
                          />
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {product.name}
                            </div>
                            <div className="text-sm text-gray-500 truncate max-w-xs">
                              {product.description}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="px-2 py-1 text-xs font-medium bg-gray-100 text-gray-800 rounded-full">
                          {product.category}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        ${product.price}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                          product.inStock 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {product.inStock ? 'In Stock' : 'Out of Stock'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {product.images ? product.images.length : 0} images
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-xs text-gray-500">
                          {product.filterValues && Object.keys(product.filterValues).length > 0 ? (
                            <div className="space-y-1">
                              {Object.entries(product.filterValues).map(([filterName, values]) => (
                                <div key={filterName}>
                                  <span className="font-medium">{filterName}:</span> {values.join(', ')}
                                </div>
                              ))}
                            </div>
                          ) : (
                            <span className="text-gray-400">No filters</span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => setEditingProduct(product)}
                            className="text-blue-600 hover:text-blue-900 p-1 hover:bg-blue-50 rounded"
                          >
                            <Edit className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => product.id && handleDelete(product.id)}
                            className="text-red-600 hover:text-red-900 p-1 hover:bg-red-50 rounded"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {(editingProduct || isCreating) && (
            <ProductForm
              product={editingProduct || emptyProduct}
              onSave={handleSave}
              onCancel={() => {
                setEditingProduct(null);
                setIsCreating(false);
              }}
            />
          )}
        </div>
      )}
    </div>
  );
};

export default AdminPanel;