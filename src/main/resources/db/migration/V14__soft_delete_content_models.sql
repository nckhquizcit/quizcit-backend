-- Soft delete support for content models: subjects, chapters, theories.

ALTER TABLE subjects
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE chapters
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE theories
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Replace global unique constraint on course_code with partial index for active subjects
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'subjects_course_code_key'
    ) THEN
        ALTER TABLE subjects DROP CONSTRAINT subjects_course_code_key;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_subjects_active_course_code
    ON subjects (LOWER(BTRIM(course_code)))
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_subjects_active_display_order
    ON subjects (display_order, created_at, subject_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_chapters_active_subject_display_order
    ON chapters (subject_id, display_order, created_at, chapter_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_theories_active_chapter
    ON theories (chapter_id, created_at, theory_id)
    WHERE deleted_at IS NULL;
