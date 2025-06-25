/*
  # Remove username column and update user authentication

  1. Database Changes
    - Drop username column from usr table
    - Update existing data to use email as primary identifier
    - Update sample users to remove username references

  2. Security
    - Maintain existing authentication structure
    - Preserve user data integrity
*/

-- Remove username column from users table
ALTER TABLE usr DROP COLUMN IF EXISTS username;

-- Update sample data to remove username references and use email-based authentication
-- Update admin user
UPDATE usr 
SET email = 'admin@catalog.com' 
WHERE id = 1 AND email != 'admin@catalog.com';

-- Update regular user  
UPDATE usr 
SET email = 'user@catalog.com' 
WHERE id = 2 AND email != 'user@catalog.com';