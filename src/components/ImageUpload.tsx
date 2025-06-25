import React, { useState, useRef } from 'react';
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

const ImageUpload: React.FC<ImageUploadProps> = ({ productId, images, token, onImagesChange }) => {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

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
    
    try {
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
        onImagesChange();
      } else {
        const errorData = await response.json();
        alert(errorData.message || 'Failed to upload image');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('Failed to upload image');
    } finally {
      setUploading(false);
    }
  };

  const deleteImage = async (imageId: number) => {
    if (!confirm('Are you sure you want to delete this image?')) return;

    try {
      const response = await fetch(`/api/admin/products/${productId}/images/${imageId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        onImagesChange();
      } else {
        alert('Failed to delete image');
      }
    } catch (error) {
      console.error('Delete error:', error);
      alert('Failed to delete image');
    }
  };

  const setPrimaryImage = async (imageId: number) => {
    try {
      const response = await fetch(`/api/admin/products/${productId}/images/${imageId}/primary`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        onImagesChange();
      } else {
        alert('Failed to set primary image');
      }
    } catch (error) {
      console.error('Set primary error:', error);
      alert('Failed to set primary image');
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
        <span className="text-sm text-gray-500">{images.length}/5 images</span>
      </div>

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
                onClick={() => fileInputRef.current?.click()}
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
                  />
                </div>
                
                {/* Overlay */}
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all duration-200 rounded-lg flex items-center justify-center">
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex space-x-2">
                    {!image.isPrimary && (
                      <button
                        onClick={() => setPrimaryImage(image.id)}
                        className="p-2 bg-white rounded-full shadow-md hover:bg-gray-50 transition-colors"
                        title="Set as primary image"
                      >
                        <Star className="h-4 w-4 text-gray-700" />
                      </button>
                    )}
                    
                    <button
                      onClick={() => deleteImage(image.id)}
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

      {images.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <ImageIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
          <p>No images uploaded yet</p>
          <p className="text-sm">Upload your first product image</p>
        </div>
      )}
    </div>
  );
};

export default ImageUpload;