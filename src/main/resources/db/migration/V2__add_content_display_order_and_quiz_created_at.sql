ALTER TABLE subjects ADD COLUMN display_order INTEGER;
ALTER TABLE chapters ADD COLUMN display_order INTEGER;
ALTER TABLE quizzes ADD COLUMN display_order INTEGER;
ALTER TABLE quizzes ADD COLUMN created_at TIMESTAMP WITH TIME ZONE;

-- Preserve the existing subject order by creation time.
WITH ranked AS (
    SELECT subject_id,
           ROW_NUMBER() OVER (ORDER BY created_at NULLS LAST, subject_id) AS position
    FROM subjects
)
UPDATE subjects AS subject
SET display_order = ranked.position
FROM ranked
WHERE subject.subject_id = ranked.subject_id;

-- Chapter order is local to each subject and follows creation time.
WITH ranked AS (
    SELECT chapter_id,
           ROW_NUMBER() OVER (
               PARTITION BY subject_id
               ORDER BY created_at NULLS LAST, chapter_id
           ) AS position
    FROM chapters
)
UPDATE chapters AS chapter
SET display_order = ranked.position
FROM ranked
WHERE chapter.chapter_id = ranked.chapter_id;

-- Existing quizzes have no timestamp. Infer a useful learning order from the
-- first number in their name (for example "Bài 2" before "Bài 10"), then use
-- the normalized name and id as deterministic fallbacks.
WITH ranked AS (
    SELECT quiz_id,
           ROW_NUMBER() OVER (
               PARTITION BY chapter_id
               ORDER BY
                   CASE WHEN name ~ '[0-9]+' THEN 0 ELSE 1 END,
                   CASE
                       WHEN name ~ '[0-9]+'
                       THEN (regexp_match(name, '[0-9]+'))[1]::BIGINT
                       ELSE NULL
                   END NULLS LAST,
                   LOWER(name),
                   quiz_id
           ) AS position
    FROM quizzes
), ordered AS (
    SELECT quiz_id,
           position,
           TIMESTAMPTZ '2000-01-01 00:00:00+00'
               + (ROW_NUMBER() OVER (ORDER BY quiz_id) * INTERVAL '1 second') AS inferred_created_at
    FROM ranked
)
UPDATE quizzes AS quiz
SET display_order = ordered.position,
    created_at = ordered.inferred_created_at
FROM ordered
WHERE quiz.quiz_id = ordered.quiz_id;

ALTER TABLE subjects ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE chapters ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE quizzes ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE quizzes ALTER COLUMN created_at SET NOT NULL;

CREATE INDEX idx_subjects_display_order ON subjects (display_order, created_at, subject_id);
CREATE INDEX idx_chapters_subject_display_order ON chapters (subject_id, display_order, created_at, chapter_id);
CREATE INDEX idx_quizzes_chapter_display_order ON quizzes (chapter_id, display_order, created_at, quiz_id);
