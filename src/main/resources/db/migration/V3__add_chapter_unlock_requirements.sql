CREATE TABLE IF NOT EXISTS chapter_skills (
    chapter_id VARCHAR(50) NOT NULL,
    skill_id VARCHAR(50) NOT NULL,
    required_theta DOUBLE PRECISION NOT NULL DEFAULT 0,
    PRIMARY KEY (chapter_id, skill_id)
);

CREATE TABLE IF NOT EXISTS user_unlocked_chapters (
    user_id VARCHAR(50) NOT NULL,
    chapter_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, chapter_id)
);

-- Existing chapters inherit the distinct skills used by their questions.  A
-- zero threshold preserves access to existing learning content.
INSERT INTO chapter_skills (chapter_id, skill_id, required_theta)
SELECT DISTINCT q.chapter_id, qs.skill_id, 0
FROM questions q
JOIN question_skills qs ON qs.question_id = q.question_id
WHERE q.chapter_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM chapter_skills cs
      WHERE cs.chapter_id = q.chapter_id AND cs.skill_id = qs.skill_id
  );

-- Some legacy databases created chapter_skills outside Flyway without its
-- composite key. Normalise duplicates before enforcing the invariant.
DELETE FROM chapter_skills first_row
USING chapter_skills duplicate_row
WHERE first_row.ctid < duplicate_row.ctid
  AND first_row.chapter_id = duplicate_row.chapter_id
  AND first_row.skill_id = duplicate_row.skill_id;

CREATE UNIQUE INDEX IF NOT EXISTS uq_chapter_skills_chapter_skill
    ON chapter_skills(chapter_id, skill_id);

CREATE INDEX IF NOT EXISTS idx_user_unlocked_chapters_chapter ON user_unlocked_chapters(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_skills_skill ON chapter_skills(skill_id);
