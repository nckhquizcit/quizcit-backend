-- U17: Rollback image_url column from subjects table
ALTER TABLE subjects DROP COLUMN IF EXISTS image_url;
