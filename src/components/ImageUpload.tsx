import React, { useState, useRef, useEffect } from 'react';
import { Upload, X, Image as ImageIcon, Star } from 'lucide-react';

interface ProductImage {
  id: number;
  imageUrl: string;
  altText: string;
  displayOrder: number;
  isPrimary: boolean;
  originalFilename: string;
}

interface ImageUploadProps {
  productId: number;
  images: ProductImage[];
  token: string;
  onImagesChange: () => void;
}

const ImageUpload: React.FC<ImageUploadProps> = ({ productId, images: initialImages, token, onImagesChange }) => {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [images, setImages] = useState<ProductImage[]>(initialImages);
  const [error, setError] = useState<string>('');
  const [fetchingImages, setFetchingImages] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Update local images when prop changes
  useEffect(() => {
    setImages(initialImages);
  }, [initialImages]);

  // Initial fetch when component mounts - only if we don't have images already
  useEffect(() => {
    if (productId && token && initialImages.length === 0 && !fetchingImages) {
      fetchImages();
    }
  }, [productId, token]); // Remove initialImages from dependencies to prevent loop

  // Fetch fresh image data with improved error handling
  const fetchImages = async (retryCount = 0) => {
    if (fetchingImages) {
      console.log('Already fetching images, skipping...');
      return;
    }

    setFetchingImages(true);
    
    try {
      console.log('Fetching images for product:', productId, 'Retry count:', retryCount);
      
      // Add cache-busting parameter to ensure fresh data
      const cacheBuster = Date.now();
      const response = await fetch(`/api/admin/products/${productId}/images?_t=${cacheBuster}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0',
        },
      });

      if (response.ok) {
        const data = await response.json();
        console.log('Successfully fetched images:', data);
        setImages(data);
        setError(''); // Clear any previous errors
        
        // Only notify parent if there's actually a change
        if (JSON.stringify(data) !== JSON.stringify(images)) {
          onImagesChange();
        }
      } else {
        const errorText = await response.text();
        console.error('Failed to fetch images:', response.status, response.statusText, errorText);
        
        let errorMessage = `Failed to fetch images (${response.status})`;
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch {
          // Use default error message if parsing fails
        }
        
        setError(errorMessage);
        
        // Retry logic for certain error codes
        if (retryCount < 2 && (response.status >= 500 || response.status === 0)) {
          console.log('Retrying fetch in 1 second...');
          setTimeout(() => fetchImages(retryCount + 1), 1000);
        }
      }
    } catch (error: any) {
      console.error('Network error while fetching images:', error);
      
      let errorMessage = 'Network error while fetching images';
      
      if (error.name === 'AbortError') {
        errorMessage = 'Request timed out while fetching images';
      } else if (error.message) {
        errorMessage = `Network error: ${error.message}`;
      }
      
      setError(errorMessage);
      
      // Retry on network errors (but not on timeout)
      if (retryCount < 2 && error.name !== 'AbortError') {
        console.log('Retrying fetch due to network error in 2 seconds...');
        setTimeout(() => fetchImages(retryCount + 1), 2000);
      }
    } finally {
      setFetchingImages(false);
    }
  };

  const handleFileSelect = (files: FileList | null) => {
    if (!files || files.length === 0) return;
    
    Array.from(files).forEach(file => {
      uploadImage(file);
    });
  };

  const uploadImage = async (file: File) => {
    if (images.length >= 5) {
      alert('Maximum 5 images allowed per product');
      return;
    }

    setUploading(true);
    setError('');
    
    try {
      console.log('Uploading image:', file.name);
      
      const formData = new FormData();
      formData.append('file', file);
      formData.append('altText', file.name);
      formData.append('displayOrder', images.length.toString());
      
      // Make first image primary if no images exist
      if (images.length === 0) {
        formData.append('isPrimary', 'true');
      }

      const response = await fetch(`/api/admin/products/${productId}/images`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      if (response.ok) {
        const newImage = await response.json();
        console.log('Image uploaded successfully:', newImage);
        
        // Update local state immediately
        setImages(prev => [...prev, newImage]);
        
        // Notify parent of change
        onImagesChange();
        
      } else {
        const errorText = await response.text();
        let errorMessage = 'Failed to upload image';
        
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = errorText || errorMessage;
        }
        
        console.error('Upload failed:', response.status, errorMessage);
        setError(errorMessage);
        alert(errorMessage);
      }
    } catch (error: any) {
      console.error('Upload error:', error);
      
      let errorMessage = 'Network error during upload';
      if (error.message) {
        errorMessage = `Upload error: ${error.message}`;
      }
      
      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setUploading(false);
    }
  };

  const deleteImage = async (imageId: number, event?: React.MouseEvent) => {
    // Prevent event propagation to avoid closing the modal
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    if (!confirm('Are you sure you want to delete this image?')) return;

    setError('');

    try {
      console.log('Deleting image:', imageId);
      
      const response = await fetch(`/api/admin/products/${productId}/images/${imageId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        console.log('Image deleted successfully');
        
        // Update local state immediately
        setImages(prev => prev.filter(img => img.id !== imageId));
        
        // Notify parent of change
        onImagesChange();
        
      } else {
        const errorText = await response.text();
        let errorMessage = 'Failed to delete image';
        
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = errorText || errorMessage;
        }
        
        console.error('Delete failed:', response.status, errorMessage);
        setError(errorMessage);
        alert(errorMessage);
      }
    } catch (error: any) {
      console.error('Delete error:', error);
      
      let errorMessage = 'Network error during deletion';
      if (error.message) {
        errorMessage = `Delete error: ${error.message}`;
      }
      
      setError(errorMessage);
      alert(errorMessage);
    }
  };

  const setPrimaryImage = async (imageId: number, event?: React.MouseEvent) => {
    // Prevent event propagation to avoid closing the modal
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    setError('');
    
    try {
      console.log('Setting primary image:', imageId);
      
      const response = await fetch(`/api/admin/products/${productId}/images/${imageId}/primary`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        console.log('Primary image set successfully');
        
        // Update local state immediately
        setImages(prev => prev.map(img => ({
          ...img,
          isPrimary: img.id === imageId
        })));
        
        // Notify parent of change
        onImagesChange();
        
      } else {
        const errorText = await response.text();
        let errorMessage = 'Failed to set primary image';
        
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = errorText || errorMessage;
        }
        
        console.error('Set primary failed:', response.status, errorMessage);
        setError(errorMessage);
        alert(errorMessage);
      }
    } catch (error: any) {
      console.error('Set primary error:', error);
      
      let errorMessage = 'Network error while setting primary image';
      if (error.message) {
        errorMessage = `Set primary error: ${error.message}`;
      }
      
      setError(errorMessage);
      alert(errorMessage);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    handleFileSelect(e.dataTransfer.files);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h4 className="text-lg font-medium text-gray-900">Product Images</h4>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-500">{images.length}/5 images</span>
          {fetchingImages && (
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
          )}
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
          <div className="flex items-center justify-between">
            <span>{error}</span>
            <button
              onClick={() => setError('')}
              className="text-red-500 hover:text-red-700"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Retry Button */}
      {error && !fetchingImages && (
        <div className="text-center">
          <button
            onClick={() => fetchImages()}
            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            Retry Loading Images
          </button>
        </div>
      )}

      {/* Upload Area */}
      {images.length < 5 && (
        <div
          className={`border-2 border-dashed rounded-lg p-6 text-center transition-colors ${
            dragOver
              ? 'border-blue-500 bg-blue-50'
              : 'border-gray-300 hover:border-gray-400'
          }`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept="image/*"
            onChange={(e) => handleFileSelect(e.target.files)}
            className="hidden"
          />
          
          <div className="space-y-2">
            <Upload className="h-8 w-8 text-gray-400 mx-auto" />
            <div>
              <button
                type="button"
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  fileInputRef.current?.click();
                }}
                className="text-blue-600 hover:text-blue-700 font-medium"
                disabled={uploading}
              >
                {uploading ? 'Uploading...' : 'Click to upload'}
              </button>
              <span className="text-gray-500"> or drag and drop</span>
            </div>
            <p className="text-xs text-gray-500">PNG, JPG, GIF up to 10MB</p>
          </div>
        </div>
      )}

      {/* Image Grid */}
      {images.length > 0 && (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {images
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .map((image) => (
              <div key={image.id} className="relative group">
                <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
                  <img
                    src={image.imageUrl}
                    alt={image.altText}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      console.error('Image failed to load:', image.imageUrl);
                    }}
                  />
                </div>
                
                {/* Overlay */}
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all duration-200 rounded-lg flex items-center justify-center">
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex space-x-2">
                    {!image.isPrimary && (
                      <button
                        type="button"
                        onClick={(e) => setPrimaryImage(image.id, e)}
                        className="p-2 bg-white rounded-full shadow-md hover:bg-gray-50 transition-colors"
                        title="Set as primary image"
                      >
                        <Star className="h-4 w-4 text-gray-700" />
                      </button>
                    )}
                    
                    <button
                      type="button"
                      onClick={(e) => deleteImage(image.id, e)}
                      className="p-2 bg-white rounded-full shadow-md hover:bg-gray-50 transition-colors"
                      title="Delete image"
                    >
                      <X className="h-4 w-4 text-red-600" />
                    </button>
                  </div>
                </div>
                
                {/* Primary Badge */}
                {image.isPrimary && (
                  <div className="absolute top-2 left-2">
                    <span className="bg-blue-600 text-white text-xs px-2 py-1 rounded-full flex items-center space-x-1">
                      <Star className="h-3 w-3 fill-current" />
                      <span>Primary</span>
                    </span>
                  </div>
                )}
                
                {/* Image Info */}
                <div className="mt-2">
                  <p className="text-xs text-gray-600 truncate" title={image.originalFilename}>
                    {image.originalFilename}
                  </p>
                </div>
              </div>
            ))}
        </div>
      )}

      {images.length === 0 && !fetchingImages && !error && (
        <div className="text-center py-8 text-gray-500">
          <ImageIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
          <p>No images uploaded yet</p>
          <p className="text-sm">Upload your first product image</p>
        </div>
      )}

      {/* Loading indicator */}
      {(uploading || fetchingImages) && (
        <div className="text-center py-4">
          <div className="inline-flex items-center space-x-2 text-blue-600">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
            <span className="text-sm">
              {uploading ? 'Uploading image...' : 'Loading images...'}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ImageUpload;