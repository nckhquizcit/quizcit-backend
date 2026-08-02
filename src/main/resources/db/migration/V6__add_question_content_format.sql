-- Cho phép câu hỏi lưu nội dung dạng rich-text (BlockNote JSON) bên cạnh
-- plain text hiện có; mặc định "plain" để tương thích ngược với dữ liệu cũ.
ALTER TABLE questions ADD COLUMN IF NOT EXISTS content_format VARCHAR(20) NOT NULL DEFAULT 'plain';
