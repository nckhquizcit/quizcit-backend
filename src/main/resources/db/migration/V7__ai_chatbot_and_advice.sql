-- V__ai_chatbot_and_advice.sql
-- Đổi tên file theo đúng số thứ tự Flyway tiếp theo trong dự án của bạn,
-- ví dụ V21__ai_chatbot_and_advice.sql (xem file migration mới nhất hiện có
-- để lấy đúng số kế tiếp).

-- 1. Bật extension pgvector (chỉ cần chạy 1 lần cho toàn bộ database).
CREATE EXTENSION IF NOT EXISTS vector;

-- Legacy databases created by Hibernate may be missing database-level
-- uniqueness for these entity @Id columns. PostgreSQL requires referenced
-- columns to be unique before a foreign key can target them.
CREATE UNIQUE INDEX IF NOT EXISTS ux_theories_theory_id
    ON theories (theory_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_chapters_chapter_id
    ON chapters (chapter_id);

-- 2. Bảng lưu các đoạn (chunk) nội dung lý thuyết đã được embedding, dùng
--    cho chatbox RAG. embedding có 768 chiều, khớp với
--    EMBEDDING_DIM trong ai-service/rag_chat.py (model text-embedding-004,
--    output_dimensionality=768).
CREATE TABLE IF NOT EXISTS theory_chunks (
    chunk_id      VARCHAR(50)  PRIMARY KEY,
    theory_id     VARCHAR(50)  NOT NULL REFERENCES theories(theory_id) ON DELETE CASCADE,
    chapter_id    VARCHAR(50)  NOT NULL REFERENCES chapters(chapter_id) ON DELETE CASCADE,
    chunk_index   INT          NOT NULL,
    content       TEXT         NOT NULL,
    embedding     vector(768)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Lọc theo chapter_id (giới hạn RAG theo chương đã mở khóa) — cần index
-- thường trước, ivfflat chỉ tăng tốc phần ORDER BY <=>.
CREATE INDEX IF NOT EXISTS idx_theory_chunks_chapter
    ON theory_chunks (chapter_id);

CREATE INDEX IF NOT EXISTS idx_theory_chunks_theory
    ON theory_chunks (theory_id);

-- Index ANN cho cosine distance (<=>). "lists = 100" phù hợp cho vài chục
-- ngàn chunk trở xuống — dữ liệu lý thuyết môn học thường không quá lớn,
-- nếu sau này > 100k chunk thì tăng "lists" theo khuyến nghị của pgvector
-- (thường sqrt(số dòng)).
CREATE INDEX IF NOT EXISTS idx_theory_chunks_embedding
    ON theory_chunks USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- 3. Cột lưu lời khuyên cải thiện (AI sinh 1 lần lúc nộp bài, không tính lại
--    mỗi lần xem kết quả).
ALTER TABLE attempts ADD COLUMN IF NOT EXISTS advice_text TEXT;
