import React, { useState, useEffect } from 'react';
import { X, ChevronLeft, ChevronRight, Package, Phone, Mail, Share2, Copy, Check } from 'lucide-react';

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
  primaryImageUrl?: string;
  inStock: boolean;
  images?: ProductImage[];
  filterValues?: Record<string, string[]>;
}

interface ProductDetailsProps {
  productId: number;
  onClose: () => void;
  formatPrice: (price: number) => string;
}

const ProductDetails: React.FC<ProductDetailsProps> = ({ productId, onClose, formatPrice }) => {
  const [product, setProduct] = useState<Product | null>(null);
  const [productImages, setProductImages] = useState<ProductImage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [showShareMenu, setShowShareMenu] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);

  useEffect(() => {
    fetchProduct();
    fetchProductImages();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      const response = await fetch(`/api/products/${productId}`);
      if (response.ok) {
        const data = await response.json();
        setProduct(data);
      } else {
        setError('Product not found');
      }
    } catch (err) {
      setError('Failed to load product');
    }
  };

  const fetchProductImages = async () => {
    try {
      // Use the public endpoint for viewing product images
      const response = await fetch(`/api/products/${productId}/images`);
      if (response.ok) {
        const images = await response.json();
        setProductImages(images);
        
        // Set initial selected image to primary image or first image
        if (images.length > 0) {
          const primaryIndex = images.findIndex((img: ProductImage) => img.isPrimary);
          setSelectedImageIndex(primaryIndex >= 0 ? primaryIndex : 0);
        }
      }
    } catch (err) {
      console.error('Failed to load product images:', err);
      // Don't set error here as images are optional
    } finally {
      setLoading(false);
    }
  };

  // Get placeholder image based on category - drilling and mining themed
  const getPlaceholderImage = (category: string) => {
    const placeholders = {
      'drilling': 'https://images.pexels.com/photos/162568/oil-rig-sea-oil-drilling-162568.jpeg?auto=compress&cs=tinysrgb&w=500',
      'mining': 'https://images.pexels.com/photos/1108572/pexels-photo-1108572.jpeg?auto=compress&cs=tinysrgb&w=500',
      'equipment': 'https://images.pexels.com/photos/1108101/pexels-photo-1108101.jpeg?auto=compress&cs=tinysrgb&w=500',
      'safety': 'https://images.pexels.com/photos/1108117/pexels-photo-1108117.jpeg?auto=compress&cs=tinysrgb&w=500',
      'tools': 'https://images.pexels.com/photos/162553/keys-workshop-mechanic-tools-162553.jpeg?auto=compress&cs=tinysrgb&w=500',
      'machinery': 'https://images.pexels.com/photos/1108572/pexels-photo-1108572.jpeg?auto=compress&cs=tinysrgb&w=500',
      'parts': 'https://images.pexels.com/photos/159298/gears-cogs-machine-machinery-159298.jpeg?auto=compress&cs=tinysrgb&w=500',
      'default': 'https://images.pexels.com/photos/162568/oil-rig-sea-oil-drilling-162568.jpeg?auto=compress&cs=tinysrgb&w=500'
    };
    
    return placeholders[category.toLowerCase()] || placeholders['default'];
  };

  const getDisplayImages = () => {
    if (productImages && productImages.length > 0) {
      return productImages.sort((a, b) => a.displayOrder - b.displayOrder);
    }
    if (product?.primaryImageUrl) {
      return [{ id: 0, imageUrl: product.primaryImageUrl, altText: product.name, displayOrder: 0, isPrimary: true, originalFilename: product.name }];
    }
    // Return placeholder image
    if (product) {
      const placeholderUrl = getPlaceholderImage(product.category);
      return [{ id: 0, imageUrl: placeholderUrl, altText: product.name, displayOrder: 0, isPrimary: true, originalFilename: 'Placeholder' }];
    }
    return [];
  };

  const handlePrevImage = () => {
    const images = getDisplayImages();
    setSelectedImageIndex(prev => prev > 0 ? prev - 1 : images.length - 1);
  };

  const handleNextImage = () => {
    const images = getDisplayImages();
    setSelectedImageIndex(prev => prev < images.length - 1 ? prev + 1 : 0);
  };

  const generateShareableLink = () => {
    const baseUrl = window.location.origin;
    return `${baseUrl}?product=${productId}`;
  };

  const copyToClipboard = async () => {
    try {
      const shareableLink = generateShareableLink();
      await navigator.clipboard.writeText(shareableLink);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    } catch (err) {
      console.error('Failed to copy to clipboard:', err);
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = generateShareableLink();
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    }
  };

  const shareViaEmail = () => {
    const shareableLink = generateShareableLink();
    const subject = encodeURIComponent(`Check out this product: ${product?.name}`);
    const body = encodeURIComponent(`I thought you might be interested in this product:\n\n${product?.name}\n${formatPrice(product?.price || 0)}\n\n${shareableLink}`);
    window.open(`mailto:?subject=${subject}&body=${body}`);
  };

  const shareViaWhatsApp = () => {
    const shareableLink = generateShareableLink();
    const text = encodeURIComponent(`Check out this product: ${product?.name}\n${formatPrice(product?.price || 0)}\n\n${shareableLink}`);
    window.open(`https://wa.me/?text=${text}`);
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="text-center mt-4 text-gray-600">Loading product...</p>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
          <div className="text-center">
            <div className="w-16 h-16 mx-auto mb-4 bg-red-100 rounded-full flex items-center justify-center">
              <X className="h-8 w-8 text-red-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Error</h3>
            <p className="text-gray-600 mb-4">{error}</p>
            <button
              onClick={onClose}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  const displayImages = getDisplayImages();
  const currentImage = displayImages[selectedImageIndex];
  const hasUploadedImages = productImages && productImages.length > 0;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
          <h2 className="text-2xl font-bold text-gray-900">Product Details</h2>
          <div className="flex items-center space-x-2">
            {/* Share Button */}
            <div className="relative">
              <button
                onClick={() => setShowShareMenu(!showShareMenu)}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors flex items-center space-x-2 text-gray-600 hover:text-blue-600"
              >
                <Share2 className="h-5 w-5" />
                <span className="text-sm font-medium">Share</span>
              </button>
              
              {showShareMenu && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-20">
                  <div className="px-4 py-2 border-b border-gray-100">
                    <p className="text-sm font-medium text-gray-900">Share this product</p>
                  </div>
                  
                  <button
                    onClick={copyToClipboard}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-3"
                  >
                    {copySuccess ? (
                      <Check className="h-4 w-4 text-green-600" />
                    ) : (
                      <Copy className="h-4 w-4" />
                    )}
                    <span>{copySuccess ? 'Copied!' : 'Copy link'}</span>
                  </button>
                  
                  <button
                    onClick={shareViaEmail}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-3"
                  >
                    <Mail className="h-4 w-4" />
                    <span>Share via Email</span>
                  </button>
                  
                  <button
                    onClick={shareViaWhatsApp}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-3"
                  >
                    <Phone className="h-4 w-4" />
                    <span>Share via WhatsApp</span>
                  </button>
                  
                  <div className="px-4 py-2 border-t border-gray-100">
                    <p className="text-xs text-gray-500">Share link:</p>
                    <p className="text-xs text-gray-600 break-all bg-gray-50 p-2 rounded mt-1">
                      {generateShareableLink()}
                    </p>
                  </div>
                </div>
              )}
            </div>
            
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-6 w-6 text-gray-500" />
            </button>
          </div>
        </div>

        {/* Click outside to close share menu */}
        {showShareMenu && (
          <div 
            className="fixed inset-0 z-10" 
            onClick={() => setShowShareMenu(false)}
          />
        )}

        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Image Gallery */}
            <div className="space-y-4">
              {/* Main Image */}
              <div className="relative bg-gray-100 rounded-xl overflow-hidden aspect-square">
                {currentImage && (
                  <img
                    src={currentImage.imageUrl}
                    alt={currentImage.altText || product.name}
                    className="w-full h-full object-cover"
                  />
                )}
                
                {displayImages.length > 1 && (
                  <>
                    <button
                      onClick={handlePrevImage}
                      className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white bg-opacity-80 hover:bg-opacity-100 rounded-full p-2 transition-all"
                    >
                      <ChevronLeft className="h-5 w-5 text-gray-700" />
                    </button>
                    <button
                      onClick={handleNextImage}
                      className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white bg-opacity-80 hover:bg-opacity-100 rounded-full p-2 transition-all"
                    >
                      <ChevronRight className="h-5 w-5 text-gray-700" />
                    </button>
                  </>
                )}

                {!product.inStock && (
                  <div className="absolute top-4 left-4">
                    <span className="bg-red-500 text-white px-3 py-1 rounded-full text-sm font-medium">
                      Out of Stock
                    </span>
                  </div>
                )}

                {!hasUploadedImages && (
                  <div className="absolute top-4 right-4">
                    <div className="bg-gray-800 bg-opacity-75 text-white text-xs px-2 py-1 rounded-full flex items-center space-x-1">
                      <Package className="h-3 w-3" />
                      <span>Placeholder</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Thumbnail Images */}
              {displayImages.length > 1 && (
                <div className="flex space-x-2 overflow-x-auto pb-2">
                  {displayImages.map((image, index) => (
                    <button
                      key={image.id}
                      onClick={() => setSelectedImageIndex(index)}
                      className={`flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden border-2 transition-all ${
                        index === selectedImageIndex
                          ? 'border-blue-500 ring-2 ring-blue-200'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <img
                        src={image.imageUrl}
                        alt={image.altText || `${product.name} ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Product Information */}
            <div className="space-y-6">
              {/* Basic Info */}
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <span className="text-sm bg-gray-100 text-gray-600 px-2 py-1 rounded-full">
                    {product.category}
                  </span>
                  {product.inStock && (
                    <span className="text-sm bg-green-100 text-green-700 px-2 py-1 rounded-full">
                      In Stock
                    </span>
                  )}
                </div>
                
                <h1 className="text-3xl font-bold text-gray-900 mb-4">{product.name}</h1>
                
                <div className="space-y-2">
                  <div className="text-4xl font-bold text-gray-900">
                    {formatPrice(product.price)}
                  </div>
                  <div className="text-sm text-blue-600 bg-blue-50 px-3 py-2 rounded-lg inline-block">
                    For large orders, please contact us for special pricing
                  </div>
                </div>
              </div>

              {/* Description */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
                <div className="text-gray-600 leading-relaxed whitespace-pre-line">
                  {product.description}
                </div>
              </div>

              {/* Filter Values */}
              {product.filterValues && Object.keys(product.filterValues).length > 0 && (
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Specifications</h3>
                  <div className="space-y-2">
                    {Object.entries(product.filterValues).map(([filterName, values]) => (
                      <div key={filterName} className="flex items-center space-x-2">
                        <span className="text-sm font-medium text-gray-700 capitalize min-w-20">
                          {filterName}:
                        </span>
                        <div className="flex flex-wrap gap-1">
                          {values.map((value, index) => (
                            <span
                              key={index}
                              className="text-sm bg-blue-100 text-blue-800 px-2 py-1 rounded-full"
                            >
                              {value}
                            </span>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Contact for Purchase */}
              <div className="bg-blue-50 border border-blue-200 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Interested in this product?</h3>
                <p className="text-gray-600 mb-4">
                  Contact our sales team for pricing, availability, and technical specifications.
                </p>
                
                <div className="flex flex-col sm:flex-row gap-3">
                  <a
                    href="tel:+97312345678"
                    className="flex items-center justify-center space-x-2 bg-blue-600 text-white px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    <Phone className="h-4 w-4" />
                    <span>Call Now</span>
                  </a>
                  <a
                    href={`mailto:sales@industrialcatalog.bh?subject=Inquiry about ${product.name}`}
                    className="flex items-center justify-center space-x-2 bg-white text-blue-600 border border-blue-600 px-4 py-3 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    <Mail className="h-4 w-4" />
                    <span>Send Email</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetails;