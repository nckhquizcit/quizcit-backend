-- Table tracking the number of completed standard quizzes before unlocking adaptive quiz per subject.

CREATE TABLE IF NOT EXISTS user_adaptive_quiz_trackings (
    user_id                     VARCHAR(50) NOT NULL,
    subject_id                  VARCHAR(50) NOT NULL,
    completed_standard_quizzes  INTEGER NOT NULL DEFAULT 0,
    required_standard_quizzes   INTEGER NOT NULL DEFAULT 2,
    last_adaptive_completed_at  TIMESTAMPTZ,
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, subject_id),
    CONSTRAINT fk_user_adaptive_trackings_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_adaptive_trackings_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_adaptive_trackings_lookup
    ON user_adaptive_quiz_trackings (user_id, subject_id);
