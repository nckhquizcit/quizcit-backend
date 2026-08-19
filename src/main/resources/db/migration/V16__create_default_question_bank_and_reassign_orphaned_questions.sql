-- Create default Subject and Chapter named "Ngân hàng câu hỏi" if not already existing,
-- and reassign any questions that have null or orphaned subject/chapter.

DO $$
DECLARE
    v_bank_subject_id VARCHAR(50);
    v_bank_chapter_id VARCHAR(50);
    v_display_order INT;
BEGIN
    -- 1. Check or insert default Subject "Ngân hàng câu hỏi"
    SELECT subject_id INTO v_bank_subject_id
    FROM subjects
    WHERE (course_code = 'QUESTION_BANK' OR name = 'Ngân hàng câu hỏi')
      AND deleted_at IS NULL
    LIMIT 1;

    IF v_bank_subject_id IS NULL THEN
        SELECT COALESCE(MAX(display_order) + 1, 1) INTO v_display_order
        FROM subjects
        WHERE deleted_at IS NULL;

        v_bank_subject_id := gen_random_uuid()::text;

        INSERT INTO subjects (subject_id, course_code, name, description, is_visible, display_order, created_at)
        VALUES (v_bank_subject_id, 'QUESTION_BANK', 'Ngân hàng câu hỏi', 'Ngân hàng câu hỏi mặc định', true, v_display_order, NOW());
    END IF;

    -- 2. Check or insert default Chapter "Ngân hàng câu hỏi"
    SELECT chapter_id INTO v_bank_chapter_id
    FROM chapters
    WHERE subject_id = v_bank_subject_id
      AND name = 'Ngân hàng câu hỏi'
      AND deleted_at IS NULL
    LIMIT 1;

    IF v_bank_chapter_id IS NULL THEN
        v_bank_chapter_id := gen_random_uuid()::text;

        INSERT INTO chapters (chapter_id, subject_id, name, display_order, created_at)
        VALUES (v_bank_chapter_id, v_bank_subject_id, 'Ngân hàng câu hỏi', 1, NOW());
    END IF;

    -- 3. Reassign any orphaned questions (where subject_id IS NULL or chapter_id IS NULL
    -- or subject_id/chapter_id references non-existent / deleted entities)
    UPDATE questions
    SET subject_id = v_bank_subject_id,
        chapter_id = v_bank_chapter_id
    WHERE subject_id IS NULL
       OR chapter_id IS NULL
       OR subject_id NOT IN (SELECT subject_id FROM subjects WHERE deleted_at IS NULL)
       OR chapter_id NOT IN (SELECT chapter_id FROM chapters WHERE deleted_at IS NULL);

END $$;
