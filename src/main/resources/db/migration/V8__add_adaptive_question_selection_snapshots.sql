CREATE TABLE adaptive_question_selections (
    selection_id VARCHAR(50) PRIMARY KEY,
    attempt_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    question_id VARCHAR(50) NOT NULL,
    skill_id VARCHAR(50) NOT NULL,
    theta_at_selection DOUBLE PRECISION NOT NULL,
    difficulty_at_selection DOUBLE PRECISION NOT NULL,
    absolute_difference DOUBLE PRECISION NOT NULL,
    is_primary_skill BOOLEAN NOT NULL,
    is_driving_skill BOOLEAN NOT NULL,
    batch_number INTEGER NOT NULL,
    question_order INTEGER NOT NULL,
    selected_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_adaptive_selection_attempt_question_skill
        UNIQUE (attempt_id, question_id, skill_id)
);

CREATE INDEX idx_adaptive_selection_attempt
    ON adaptive_question_selections(attempt_id);

CREATE INDEX idx_adaptive_selection_user_time
    ON adaptive_question_selections(user_id, selected_at DESC);

CREATE INDEX idx_adaptive_selection_skill
    ON adaptive_question_selections(skill_id);

CREATE INDEX idx_adaptive_selection_question
    ON adaptive_question_selections(question_id);
