-- V17: Add image_url column to subjects table for subject banners / thumbnails
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
