-- Allow questions to exist in the database without being assigned to a subject or chapter.
-- When a subject or chapter is deleted, referencing questions have their subject_id and chapter_id
-- set to NULL so they are preserved in the question bank and can be reassigned later.
ALTER TABLE questions ALTER COLUMN subject_id DROP NOT NULL;
ALTER TABLE questions ALTER COLUMN chapter_id DROP NOT NULL;
