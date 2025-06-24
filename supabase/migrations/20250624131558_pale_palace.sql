-- Sample data for the catalog application
INSERT IGNORE INTO products (id, name, description, price, category, image_url, rating, in_stock, created_at, updated_at) VALUES
(1, 'Wireless Bluetooth Headphones', 'Premium quality wireless headphones with noise cancellation and 30-hour battery life', 199.99, 'electronics', 'https://images.pexels.com/photos/3394650/pexels-photo-3394650.jpeg?auto=compress&cs=tinysrgb&w=500', 4.5, true, NOW(), NOW()),
(2, 'Smart Fitness Watch', 'Advanced fitness tracking with heart rate monitor, GPS, and smartphone integration', 299.99, 'electronics', 'https://images.pexels.com/photos/437037/pexels-photo-437037.jpeg?auto=compress&cs=tinysrgb&w=500', 4.3, true, NOW(), NOW()),
(3, 'Organic Cotton T-Shirt', 'Comfortable and sustainable organic cotton t-shirt available in multiple colors', 29.99, 'clothing', 'https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=500', 4.2, true, NOW(), NOW()),
(4, 'Premium Leather Wallet', 'Handcrafted genuine leather wallet with RFID blocking technology', 79.99, 'accessories', 'https://images.pexels.com/photos/1152077/pexels-photo-1152077.jpeg?auto=compress&cs=tinysrgb&w=500', 4.7, true, NOW(), NOW()),
(5, 'Stainless Steel Water Bottle', 'Insulated stainless steel water bottle that keeps drinks cold for 24 hours', 34.99, 'home', 'https://images.pexels.com/photos/1000084/pexels-photo-1000084.jpeg?auto=compress&cs=tinysrgb&w=500', 4.4, true, NOW(), NOW()),
(6, 'Yoga Mat Pro', 'Non-slip premium yoga mat with alignment guides and carrying strap', 59.99, 'sports', 'https://images.pexels.com/photos/3822864/pexels-photo-3822864.jpeg?auto=compress&cs=tinysrgb&w=500', 4.6, true, NOW(), NOW()),
(7, 'Wireless Phone Charger', 'Fast wireless charging pad compatible with all Qi-enabled devices', 49.99, 'electronics', 'https://images.pexels.com/photos/4526414/pexels-photo-4526414.jpeg?auto=compress&cs=tinysrgb&w=500', 4.1, true, NOW(), NOW()),
(8, 'Ceramic Coffee Mug Set', 'Set of 4 handcrafted ceramic coffee mugs with unique glazed finish', 39.99, 'home', 'https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg?auto=compress&cs=tinysrgb&w=500', 4.3, true, NOW(), NOW()),
(9, 'Running Shoes', 'Lightweight running shoes with advanced cushioning and breathable mesh upper', 129.99, 'sports', 'https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?auto=compress&cs=tinysrgb&w=500', 4.5, false, NOW(), NOW()),
(10, 'Artisan Dark Chocolate', 'Premium single-origin dark chocolate bar with 70% cocoa content', 12.99, 'food', 'https://images.pexels.com/photos/918327/pexels-photo-918327.jpeg?auto=compress&cs=tinysrgb&w=500', 4.8, true, NOW(), NOW());

-- Create default admin user (password: admin123)
INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) VALUES
(1, 'admin', 'admin@catalog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'User', 'ADMIN', true, NOW(), NOW());

-- Create default regular user (password: user123)
INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) VALUES
(2, 'user', 'user@catalog.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Regular', 'User', 'USER', true, NOW(), NOW());