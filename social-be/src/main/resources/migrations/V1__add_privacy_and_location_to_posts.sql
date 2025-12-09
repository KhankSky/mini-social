-- Migration: Add privacy and location columns to posts table
-- Version: 1
-- Date: 2025-01-XX
-- Description: 
--   - Add privacy column (VARCHAR, NOT NULL, DEFAULT 'PUBLIC') to store post privacy setting
--   - Add location column (VARCHAR, NULLABLE) to store post location

-- Check if columns already exist before adding
SET @dbname = DATABASE();
SET @tablename = 'posts';
SET @columnname1 = 'privacy';
SET @columnname2 = 'location';

-- Add privacy column if not exists
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname1)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname1, ' VARCHAR(20) NOT NULL DEFAULT ''PUBLIC''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add location column if not exists
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname2)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname2, ' VARCHAR(255)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update existing posts to have PUBLIC privacy if NULL (safety check)
UPDATE posts SET privacy = 'PUBLIC' WHERE privacy IS NULL;

