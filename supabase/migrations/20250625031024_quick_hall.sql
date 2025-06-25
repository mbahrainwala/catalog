/*
  # Create product_images table

  1. New Tables
    - `product_images`
      - `id` (bigint, primary key, auto increment)
      - `product_id` (bigint, foreign key to products table)
      - `image_url` (varchar, not null)
      - `original_filename` (varchar, not null)
      - `alt_text` (varchar, nullable)
      - `display_order` (integer, default 0)
      - `is_primary` (boolean, default false)
      - `file_size` (bigint, nullable)
      - `content_type` (varchar, nullable)
      - `created_at` (timestamp, not null)
      - `updated_at` (timestamp, nullable)

  2. Indexes
    - Index on product_id for faster queries
    - Index on is_primary for primary image lookups
    - Index on display_order for sorting

  3. Constraints
    - Foreign key constraint to products table
    - Only one primary image per product (handled in application logic)
*/

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    file_size BIGINT,
    content_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_product_images_product_id 
        FOREIGN KEY (product_id) REFERENCES products(id) 
        ON DELETE CASCADE,
    
    INDEX idx_product_images_product_id (product_id),
    INDEX idx_product_images_is_primary (is_primary),
    INDEX idx_product_images_display_order (display_order)
);