-- Soft delete columns and active indexes for Question, Quiz, Role, and Permission.

ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE quizzes
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Recreate unique index for roles with partial filter
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'uk_roles_name'
    ) THEN
        DROP INDEX uk_roles_name;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_active_name
    ON roles (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_permissions_active_name
    ON permissions (LOWER(BTRIM(name)))
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_questions_active
    ON questions (chapter_id, subject_id, created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_quizzes_active
    ON quizzes (chapter_id, display_order, created_at)
    WHERE deleted_at IS NULL;
