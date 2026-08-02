-- Flyway Teams: add db/rollback to locations before running `flyway undo`.
-- Flyway Community: execute this compensating migration explicitly when rolling back release V1.
ALTER TABLE subjects
    DROP COLUMN IF EXISTS is_visible;
