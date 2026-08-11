-- Developed for and verified against the local Docker dev database first.
-- Production deployment must go through the reviewed Flyway release process.

-- Schema hiện tại khai báo hai cột ID là NOT NULL nhưng thiếu unique/primary
-- constraint. Hai unique index này vừa bảo vệ định danh, vừa là khóa đích hợp
-- lệ cho foreign key của bảng enrollment.
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_user_id
    ON users (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_subjects_subject_id
    ON subjects (subject_id);

CREATE TABLE user_course_enrollments (
    user_id     VARCHAR(50) NOT NULL,
    subject_id  VARCHAR(50) NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, subject_id),
    CONSTRAINT fk_course_enrollments_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_course_enrollments_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE CASCADE
);

CREATE INDEX idx_course_enrollments_subject
    ON user_course_enrollments (subject_id, user_id);
