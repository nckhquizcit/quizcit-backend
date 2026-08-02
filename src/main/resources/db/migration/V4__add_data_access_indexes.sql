-- Index every high-frequency lookup/filter key used by the API. PostgreSQL
-- does not automatically index the referencing side of foreign keys.
CREATE INDEX IF NOT EXISTS idx_questions_chapter_id ON questions(chapter_id);
CREATE INDEX IF NOT EXISTS idx_options_question_id ON options(question_id);
CREATE INDEX IF NOT EXISTS idx_question_skills_question_id ON question_skills(question_id);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_quiz_order ON quiz_questions(quiz_id, index_order);
CREATE INDEX IF NOT EXISTS idx_quizzes_subject_id ON quizzes(subject_id);
CREATE INDEX IF NOT EXISTS idx_attempts_user_id ON attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_responses_attempt_id ON responses(attempt_id);
CREATE INDEX IF NOT EXISTS idx_user_skill_ability_user_id ON user_skill_ability(user_id);
CREATE INDEX IF NOT EXISTS idx_ability_history_user_created ON ability_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ability_history_attempt_id ON ability_history(attempt_id);
CREATE INDEX IF NOT EXISTS idx_chapter_skills_chapter_id ON chapter_skills(chapter_id);
