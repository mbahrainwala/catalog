/*
  # Enhanced User Profile Schema

  1. Database Changes
    - Add new optional fields to usr table
    - Add profile_picture_url for uploaded profile images
    - Add document storage fields for ID documents
    - Add contact information fields
    - Add address fields with country support

  2. New Fields
    - alternate_email (varchar, nullable)
    - phone_number (varchar, nullable) - includes country code
    - alternate_phone_number (varchar, nullable) - includes country code
    - address1_line1 (varchar, nullable)
    - address1_line2 (varchar, nullable)
    - address1_city (varchar, nullable)
    - address1_state (varchar, nullable)
    - address1_postal_code (varchar, nullable)
    - address1_country (varchar, nullable)
    - address2_line1 (varchar, nullable)
    - address2_line2 (varchar, nullable)
    - address2_city (varchar, nullable)
    - address2_state (varchar, nullable)
    - address2_postal_code (varchar, nullable)
    - address2_country (varchar, nullable)
    - profile_picture_url (varchar, nullable)
    - id_document1_url (varchar, nullable)
    - id_document1_filename (varchar, nullable)
    - id_document2_url (varchar, nullable)
    - id_document2_filename (varchar, nullable)

  3. Indexes
    - Index on alternate_email for lookups
    - Index on phone_number for searches
*/

-- Add new profile fields to usr table
ALTER TABLE usr 
ADD COLUMN IF NOT EXISTS alternate_email VARCHAR(255),
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS alternate_phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS address1_line1 VARCHAR(255),
ADD COLUMN IF NOT EXISTS address1_line2 VARCHAR(255),
ADD COLUMN IF NOT EXISTS address1_city VARCHAR(100),
ADD COLUMN IF NOT EXISTS address1_state VARCHAR(100),
ADD COLUMN IF NOT EXISTS address1_postal_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS address1_country VARCHAR(100),
ADD COLUMN IF NOT EXISTS address2_line1 VARCHAR(255),
ADD COLUMN IF NOT EXISTS address2_line2 VARCHAR(255),
ADD COLUMN IF NOT EXISTS address2_city VARCHAR(100),
ADD COLUMN IF NOT EXISTS address2_state VARCHAR(100),
ADD COLUMN IF NOT EXISTS address2_postal_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS address2_country VARCHAR(100),
ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(1000),
ADD COLUMN IF NOT EXISTS id_document1_url VARCHAR(1000),
ADD COLUMN IF NOT EXISTS id_document1_filename VARCHAR(255),
ADD COLUMN IF NOT EXISTS id_document2_url VARCHAR(1000),
ADD COLUMN IF NOT EXISTS id_document2_filename VARCHAR(255);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_usr_alternate_email ON usr(alternate_email);
CREATE INDEX IF NOT EXISTS idx_usr_phone_number ON usr(phone_number);