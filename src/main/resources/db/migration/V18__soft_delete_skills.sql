-- Soft delete support for skills table

ALTER TABLE skills
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Unique constraint on skill name for active skills (case-insensitive & trimmed)
CREATE UNIQUE INDEX IF NOT EXISTS uk_skills_active_name
    ON skills (LOWER(BTRIM(name)))
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_skills_active
    ON skills (skill_id)
    WHERE deleted_at IS NULL;
