import React, { useState, useEffect } from 'react';
import { Search, Filter, ShoppingBag, Eye, User, LogOut, Settings, Package } from 'lucide-react';
import LoginModal from './components/LoginModal';
import AdminPanel from './components/AdminPanel';
import ProductFilters from './components/ProductFilters';
import ProductDetails from './components/ProductDetails';

interface ProductImage {
  id: number;
  imageUrl: string;
  altText: string;
  displayOrder: number;
  isPrimary: boolean;
  originalFilename: string;
}

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  primaryImageUrl?: string; // Primary image URL from uploaded images
  inStock: boolean;
  images?: ProductImage[]; // Add images array
  filterValues?: Record<string, string[]>;
}

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

interface Category {
  id: number;
  name: string;
  description: string;
  displayOrder: number;
  active: boolean;
}

interface FilterType {
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

function App() {
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [availableFilters, setAvailableFilters] = useState<FilterType[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedFilters, setSelectedFilters] = useState<Record<string, string[]>>({});
  const [showFilters, setShowFilters] = useState(false);
  const [loading, setLoading] = useState(true);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [showAdminPanel, setShowAdminPanel] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);

  useEffect(() => {
    // Check for existing token on app load
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setToken(savedToken);
      checkAuthStatus(savedToken);
    }
    fetchProducts();
    fetchCategories();
  }, []);

  useEffect(() => {
    // Fetch filters when category changes
    fetchAvailableFilters();
  }, [selectedCategory]);

  useEffect(() => {
    // Fetch products with current filters applied
    fetchProducts();
  }, [searchTerm, selectedCategory, selectedFilters]);

  const checkAuthStatus = async (authToken: string) => {
    try {
      const response = await fetch('/api/auth/check-auth', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authToken}`,
        },
      });

      if (response.ok) {
        const userData = await response.json();
        setUser(userData);
      } else {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
      }
    } catch (err) {
      console.error('Auth check failed:', err);
      localStorage.removeItem('token');
      setToken(null);
      setUser(null);
    }
  };

  const fetchProducts = async () => {
    try {
      // Build query parameters
      const params = new URLSearchParams();
      
      if (searchTerm) {
        params.append('search', searchTerm);
      }
      
      if (selectedCategory !== 'all') {
        params.append('category', selectedCategory);
      }
      
      // Add filter parameters
      Object.entries(selectedFilters).forEach(([filterName, values]) => {
        if (values.length > 0) {
          params.append(filterName, values.join(','));
        }
      });
      
      const queryString = params.toString();
      const url = `/api/products${queryString ? `?${queryString}` : ''}`;
      
      console.log('Fetching products with URL:', url); // Debug log
      
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        setProducts(data);
        setFilteredProducts(data);
      }
    } catch (err) {
      console.error('Failed to fetch products:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await fetch('/api/categories');
      if (response.ok) {
        const data = await response.json();
        setCategories(data);
      }
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const fetchAvailableFilters = async () => {
    try {
      const categoryParam = selectedCategory !== 'all' ? `?category=${encodeURIComponent(selectedCategory)}` : '';
      const response = await fetch(`/api/filters${categoryParam}`);
      if (response.ok) {
        const data = await response.json();
        setAvailableFilters(data);
      }
    } catch (err) {
      console.error('Failed to fetch filters:', err);
    }
  };

  const handleLogin = (authToken: string, userData: any) => {
    setToken(authToken);
    setUser(userData);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    setShowAdminPanel(false);
  };

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setSelectedFilters({}); // Clear filters when category changes
    setShowFilters(false); // Hide filters panel when category changes
  };

  const handleFiltersChange = (filters: Record<string, string[]>) => {
    console.log('Filters changed:', filters); // Debug log
    setSelectedFilters(filters);
  };

  const handleProductClick = (productId: number) => {
    setSelectedProductId(productId);
  };

  // Get the display image URL for a product with placeholder fallback
  const getProductImageUrl = (product: Product) => {
    // Priority: primaryImageUrl > placeholder
    if (product.primaryImageUrl) {
      return product.primaryImageUrl;
    }
    
    // Return placeholder image based on category
    return getPlaceholderImage(product.category);
  };

  // Get placeholder image based on category
  const getPlaceholderImage = (category: string) => {
    const placeholders = {
      'electronics': 'https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?auto=compress&cs=tinysrgb&w=500',
      'clothing': 'https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=500',
      'accessories': 'https://images.pexels.com/photos/1152077/pexels-photo-1152077.jpeg?auto=compress&cs=tinysrgb&w=500',
      'home': 'https://images.pexels.com/photos/1000084/pexels-photo-1000084.jpeg?auto=compress&cs=tinysrgb&w=500',
      'sports': 'https://images.pexels.com/photos/3822864/pexels-photo-3822864.jpeg?auto=compress&cs=tinysrgb&w=500',
      'food': 'https://images.pexels.com/photos/918327/pexels-photo-918327.jpeg?auto=compress&cs=tinysrgb&w=500',
      'default': 'https://images.pexels.com/photos/230544/pexels-photo-230544.jpeg?auto=compress&cs=tinysrgb&w=500'
    };
    
    return placeholders[category.toLowerCase()] || placeholders['default'];
  };

  // Check if filters are available and have active values
  const hasAvailableFilters = availableFilters.length > 0 && 
    availableFilters.some(filter => 
      filter.active && 
      filter.filterValues && 
      filter.filterValues.length > 0 && 
      filter.filterValues.some(value => value.active)
    );

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading catalog...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-2">
              <ShoppingBag className="h-8 w-8 text-blue-600" />
              <h1 className="text-2xl font-bold text-gray-900">Catalog</h1>
            </div>
            
            <div className="flex items-center space-x-4">
              {user ? (
                <div className="flex items-center space-x-4">
                  <span className="text-sm text-gray-600">
                    Welcome, {user.firstName}
                  </span>
                  
                  {user.role === 'ROLE_ADMIN' && (
                    <button
                      onClick={() => setShowAdminPanel(!showAdminPanel)}
                      className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                        showAdminPanel
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      <Settings className="h-4 w-4 inline mr-1" />
                      Admin Panel
                    </button>
                  )}
                  
                  <button
                    onClick={handleLogout}
                    className="flex items-center space-x-1 px-3 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                  >
                    <LogOut className="h-4 w-4" />
                    <span>Logout</span>
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setShowLoginModal(true)}
                  className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                >
                  <User className="h-4 w-4" />
                  <span>Login</span>
                </button>
              )}
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {showAdminPanel && user?.role === 'ROLE_ADMIN' && token ? (
          <AdminPanel token={token} />
        ) : (
          <>
            {/* Search and Filter Section */}
            <div className="bg-white rounded-xl shadow-sm p-6 mb-8">
              <div className="flex flex-col md:flex-row gap-4">
                {/* Search */}
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                  <input
                    type="text"
                    placeholder="Search products..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  />
                </div>
                
                {/* Category Filter */}
                <div className="relative">
                  <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                  <select
                    value={selectedCategory}
                    onChange={(e) => handleCategoryChange(e.target.value)}
                    className="pl-10 pr-8 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white min-w-[150px] appearance-none transition-all"
                  >
                    <option value="all">All Categories</option>
                    {categories
                      .filter(cat => cat.active)
                      .sort((a, b) => a.displayOrder - b.displayOrder)
                      .map(category => (
                        <option key={category.id} value={category.name}>
                          {category.name}
                        </option>
                      ))}
                  </select>
                </div>

                {/* Toggle Filters Button - Only show if filters are available */}
                {hasAvailableFilters && (
                  <button
                    onClick={() => setShowFilters(!showFilters)}
                    className={`px-4 py-3 rounded-lg font-medium transition-colors ${
                      showFilters
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    <Filter className="h-4 w-4 inline mr-2" />
                    Filters
                    {Object.keys(selectedFilters).length > 0 && (
                      <span className="ml-2 bg-blue-500 text-white text-xs px-2 py-1 rounded-full">
                        {Object.values(selectedFilters).reduce((total, values) => total + values.length, 0)}
                      </span>
                    )}
                  </button>
                )}
              </div>
              
              <div className="mt-4 text-sm text-gray-600">
                {filteredProducts.length} products found
                {Object.keys(selectedFilters).length > 0 && (
                  <span className="ml-2 text-blue-600">
                    (filtered by {Object.keys(selectedFilters).length} filter{Object.keys(selectedFilters).length > 1 ? 's' : ''})
                  </span>
                )}
              </div>
            </div>

            <div className="flex gap-8">
              {/* Filters Sidebar - Only show if filters are available and toggled on */}
              {hasAvailableFilters && showFilters && (
                <div className="w-80 flex-shrink-0">
                  <ProductFilters 
                    onFiltersChange={handleFiltersChange}
                    selectedCategory={selectedCategory}
                  />
                </div>
              )}

              {/* Product Grid */}
              <div className="flex-1">
                {filteredProducts.length === 0 ? (
                  <div className="text-center py-16">
                    <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
                      <Search className="h-12 w-12 text-gray-400" />
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">No products found</h3>
                    <p className="text-gray-600">Try adjusting your search or filter criteria</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {filteredProducts.map((product) => (
                      <div
                        key={product.id}
                        className="bg-white rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden group cursor-pointer"
                        onClick={() => handleProductClick(product.id)}
                      >
                        {/* Product Image */}
                        <div className="relative overflow-hidden">
                          <img
                            src={getProductImageUrl(product)}
                            alt={product.name}
                            className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
                          />
                          <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition-all duration-300 flex items-center justify-center">
                            <div className="flex space-x-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                              <button 
                                className="p-2 bg-white rounded-full shadow-md hover:bg-gray-50 transition-colors"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleProductClick(product.id);
                                }}
                              >
                                <Eye className="h-5 w-5 text-gray-700" />
                              </button>
                            </div>
                          </div>
                          {!product.inStock && (
                            <span className="absolute top-3 left-3 px-2 py-1 bg-red-500 text-white text-xs font-medium rounded-full">
                              Out of Stock
                            </span>
                          )}
                          {!product.primaryImageUrl && (
                            <div className="absolute top-3 right-3">
                              <div className="bg-gray-800 bg-opacity-75 text-white text-xs px-2 py-1 rounded-full flex items-center space-x-1">
                                <Package className="h-3 w-3" />
                                <span>No Image</span>
                              </div>
                            </div>
                          )}
                        </div>

                        {/* Product Info */}
                        <div className="p-5">
                          <div className="flex items-start justify-between mb-2">
                            <h3 className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-2">
                              {product.name}
                            </h3>
                            <span className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-full ml-2 flex-shrink-0">
                              {product.category}
                            </span>
                          </div>
                          
                          <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                            {product.description}
                          </p>
                          
                          <div className="flex items-center justify-between">
                            <span className="text-2xl font-bold text-gray-900">
                              ${product.price}
                            </span>
                            <button 
                              disabled={!product.inStock}
                              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                                product.inStock
                                  ? 'bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md'
                                  : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                              }`}
                              onClick={(e) => {
                                e.stopPropagation();
                                // Add to cart logic here
                              }}
                            >
                              {product.inStock ? 'Add to Cart' : 'Unavailable'}
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}
      </div>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center">
            <div className="flex items-center justify-center space-x-2 mb-4">
              <ShoppingBag className="h-6 w-6 text-blue-600" />
              <span className="text-lg font-semibold text-gray-900">Catalog</span>
            </div>
            <p className="text-gray-600">Your premium product catalog experience</p>
          </div>
        </div>
      </footer>

      {/* Login Modal */}
      <LoginModal
        isOpen={showLoginModal}
        onClose={() => setShowLoginModal(false)}
        onLogin={handleLogin}
      />

      {/* Product Details Modal */}
      {selectedProductId && (
        <ProductDetails
          productId={selectedProductId}
          onClose={() => setSelectedProductId(null)}
        />
      )}
    </div>
  );
}

export default App;