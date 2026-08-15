-- Keep visible-subject listings index-only friendly without enlarging the index
-- with rows that ordinary users can never read.
CREATE INDEX IF NOT EXISTS idx_subjects_visible_display_order
    ON subjects (display_order, created_at, subject_id)
    WHERE is_visible = TRUE;

-- The content endpoint excludes adaptive quizzes and orders the remaining rows.
CREATE INDEX IF NOT EXISTS idx_quizzes_subject_non_adaptive_order
    ON quizzes (subject_id, display_order, created_at, quiz_id)
    WHERE type <> 'ADAPTIVE';

-- Legacy schemas did not consistently declare these identifiers as keys.
CREATE UNIQUE INDEX IF NOT EXISTS uq_skills_skill_id ON skills (skill_id);

-- Remove legacy orphans before enforcing referential integrity. These rows can
-- no longer participate in access checks and are therefore safe to discard.
DELETE FROM chapter_skills chapter_skill
WHERE NOT EXISTS (
    SELECT 1 FROM chapters chapter WHERE chapter.chapter_id = chapter_skill.chapter_id
);

DELETE FROM chapter_skills chapter_skill
WHERE NOT EXISTS (
    SELECT 1 FROM skills skill WHERE skill.skill_id = chapter_skill.skill_id
);

DELETE FROM user_unlocked_chapters unlocked
WHERE NOT EXISTS (
    SELECT 1 FROM users app_user WHERE app_user.user_id = unlocked.user_id
);

DELETE FROM user_unlocked_chapters unlocked
WHERE NOT EXISTS (
    SELECT 1 FROM chapters chapter WHERE chapter.chapter_id = unlocked.chapter_id
);

ALTER TABLE chapter_skills
    ADD CONSTRAINT fk_chapter_skills_chapter
    FOREIGN KEY (chapter_id) REFERENCES chapters (chapter_id) ON DELETE CASCADE;

ALTER TABLE chapter_skills
    ADD CONSTRAINT fk_chapter_skills_skill
    FOREIGN KEY (skill_id) REFERENCES skills (skill_id) ON DELETE CASCADE;

ALTER TABLE user_unlocked_chapters
    ADD CONSTRAINT fk_user_unlocked_chapters_user
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE user_unlocked_chapters
    ADD CONSTRAINT fk_user_unlocked_chapters_chapter
    FOREIGN KEY (chapter_id) REFERENCES chapters (chapter_id) ON DELETE CASCADE;
