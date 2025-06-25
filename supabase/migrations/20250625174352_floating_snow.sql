/*
  # Add Owner User

  1. New User
    - Create an owner user with email 'owner@catalog.com'
    - Password: 'abcd1234' (hashed with BCrypt)
    - Role: OWNER
    - Account is activated and enabled

  2. Security
    - Password is properly hashed using BCrypt
    - Account is immediately activated (no temporary password needed)
*/

-- Insert owner user with password 'abcd1234'
-- BCrypt hash for 'abcd1234' with strength 10
INSERT IGNORE INTO usr (id, email, password, first_name, last_name, role, enabled, is_temporary_password, account_activated, created_at, updated_at) VALUES
(3, 'owner@catalog.com', '$2a$10$N9qo8uLOickgx2ZrVzaKe.4iZGOoXeKtllApnx7kMmIpbzOcvZn3C', 'System', 'Owner', 'OWNER', true, false, true, NOW(), NOW());