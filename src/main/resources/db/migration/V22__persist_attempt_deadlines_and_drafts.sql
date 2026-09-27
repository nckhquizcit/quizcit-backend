ALTER TABLE attempts
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    ADD COLUMN deadline_at TIMESTAMPTZ,
    ADD COLUMN submitted_at TIMESTAMPTZ;

CREATE UNIQUE INDEX IF NOT EXISTS idx_attempts_attempt_id_unique
    ON attempts (attempt_id);

CREATE TABLE attempt_draft_answers (
    attempt_id VARCHAR(50) NOT NULL REFERENCES attempts(attempt_id) ON DELETE CASCADE,
    question_id VARCHAR(50) NOT NULL,
    option_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (attempt_id, question_id)
);

WITH response_counts AS (
    SELECT attempt_id, COUNT(*) AS total
    FROM responses
    GROUP BY attempt_id
), attempt_state AS (
    SELECT a.attempt_id, q.type, q.duration, COALESCE(rc.total, 0) AS response_count
    FROM attempts a
    JOIN quizzes q ON q.quiz_id = a.quiz_id
    LEFT JOIN response_counts rc ON rc.attempt_id = a.attempt_id
)
UPDATE attempts a
SET status = CASE
        WHEN s.type = 'ADAPTIVE' AND s.response_count < a.total_question THEN 'IN_PROGRESS'
        WHEN s.type <> 'ADAPTIVE' AND s.response_count = 0 THEN 'IN_PROGRESS'
        ELSE 'SUBMITTED'
    END,
    deadline_at = CASE
        WHEN s.type <> 'ADAPTIVE' AND s.response_count = 0 AND s.duration > 0
            THEN CURRENT_TIMESTAMP + make_interval(mins => s.duration)
        ELSE NULL
    END,
    submitted_at = CASE
        WHEN s.type = 'ADAPTIVE' AND s.response_count < a.total_question THEN NULL
        WHEN s.type <> 'ADAPTIVE' AND s.response_count = 0 THEN NULL
        ELSE a.created_at
    END
FROM attempt_state s
WHERE s.attempt_id = a.attempt_id;

CREATE INDEX idx_attempts_expired_in_progress
    ON attempts (deadline_at)
    WHERE status = 'IN_PROGRESS' AND deadline_at IS NOT NULL;
