# Báo cáo tối ưu truy cập dữ liệu QuizCIT

Ngày kiểm tra: 2026-08-02  
Branch backend và frontend: `perf/data-access-latency`

## 1. Phạm vi và nguyên tắc

Toàn bộ các service, repository và màn hình có truy cập dữ liệu đã được rà soát. Việc refactor giữ nguyên các quy tắc nghiệp vụ: visibility của môn học, điều kiện mở khóa chương, cách chọn câu hỏi adaptive, công thức blend theta, tiêu chí attempt hoàn thành, thứ tự hiển thị và cấu trúc dữ liệu cũ. Các endpoint cũ vẫn tồn tại; endpoint mới chỉ cung cấp cách đọc tổng hợp để loại bỏ fan-out.

RTT đo từ máy chạy backend tới PostgreSQL `34.87.73.15` là khoảng 55 ms. Vì vậy N query tuần tự tạo ra tối thiểu xấp xỉ `N × 55 ms`, dù bản thân PostgreSQL xử lý query nhanh.

## 2. Các nguyên nhân tìm thấy

- Frontend lấy danh sách cha rồi gọi một API cho từng môn/chương/user.
- Backend tải từng question, option, skill, role, attempt và ability trong vòng lặp.
- Lịch sử attempt thực hiện nhiều query cho từng attempt.
- Trang thống kê admin tải toàn bộ dữ liệu chỉ để đếm.
- Hibernate chưa bật JDBC batching; các cột khóa ngoại thường xuyên lọc chưa có đủ index.
- Các request GET trùng nhau trong cùng thời điểm, đặc biệt React development/Strict Mode, không được gộp.
- Khi AI service cold-start hoặc mất kết nối, nộp bài có thể chờ timeout dài trước khi dùng thuật toán fallback Java tương đương.

## 3. Thay đổi backend

### 3.1 Batch hóa các luồng đọc

- Tải câu hỏi trong quiz bằng một JPQL join có `ORDER BY indexOrder`, thay cho một query quan hệ cộng một query cho từng question.
- Tải options và question-skill bằng `IN (...)` cho toàn bộ danh sách question.
- Tải tên skill bằng một lần `findAllById`, không còn query từng skill.
- Tải question của nhiều chapter adaptive bằng một query `chapter_id IN (...)`.
- Tải chapter-skill của nhiều chapter adaptive bằng một query.
- Tải role của danh sách user bằng hai query cố định: assignments và roles.
- Tải responses, quizzes và subjects của lịch sử attempt theo batch. Số query không còn tăng tuyến tính theo số attempt.
- Validation khi tạo/cập nhật quiz và reorder subject/chapter/quiz dùng batch lookup theo ID.

### 3.2 Batch hóa luồng ghi

- Response của một lần submit được ghi bằng `saveAll`.
- Ability và ability-history của normal/adaptive quiz được gom và ghi bằng `saveAll`.
- Bật `hibernate.jdbc.batch_size=50`, `order_inserts` và `order_updates` để các lời gọi save trong cùng transaction thực sự được gom ở tầng JDBC.

### 3.3 API tổng hợp mới

- `GET /api/chapters`: toàn bộ chapter người hiện tại được phép thấy.
- `GET /api/questions`: ngân hàng câu hỏi cho administrator.
- `GET /api/subjects/{subjectId}/theories`: theory của các chapter được phép truy cập trong một request.
- `GET /api/admin/data-stats`: một SQL statement trả về tổng user, subject, question, quiz và completed attempt.

API thống kê dùng đúng tiêu chí cũ đối với adaptive attempt: phải có response và số response phải đạt `total_question`.

### 3.4 Connection pool và timeout

- Hikari pool có cấu hình mặc định/override rõ ràng: tối đa 15, tối thiểu idle 3, connection timeout 5 giây, validation timeout 2 giây và keepalive 30 giây.
- Tắt Open Session in View để không phát sinh lazy query ngoài service transaction.
- AI connect/read timeout mặc định giảm từ 2.000/3.000 ms xuống 500/750 ms. Khi timeout, code vẫn chạy IRT fallback Java hiện có với cùng công thức, nên không làm mất kết quả nghiệp vụ.

### 3.5 Index PostgreSQL

Flyway V4 và V5 bổ sung index cho các lookup thường xuyên:

- `questions.chapter_id`
- `options.question_id`
- `question_skills.question_id`
- `quiz_questions(quiz_id, index_order)` và `quiz_questions.question_id`
- `quizzes.subject_id`
- `attempts.user_id`
- `responses.attempt_id` và `responses.question_id`
- `user_skill_ability.user_id`
- `ability_history(user_id, created_at)` và `ability_history.attempt_id`
- `chapter_skills.chapter_id`
- `theories.chapter_id`
- `user_roles.user_id`

Migration đã chạy thành công trên PostgreSQL và schema hiện ở version 5.

## 4. Thay đổi frontend

- Trang chủ: từ `1 + số môn + 2` request xuống 4 request cố định.
- Danh sách môn: từ `1 + số môn` xuống 2 request.
- Chi tiết môn: từ `2 + 2 × số chương` xuống 4 request cố định.
- Dashboard admin: từ `2 + 2 × số môn + số chương` xuống 1 request.
- Profile admin: từ `2 + số môn + số chương + số user` xuống 1 request.
- Ngân hàng câu hỏi: từ `1 + số môn + số chương` xuống 3 request song song.
- Danh sách chapter admin: bỏ request question cho từng chapter; dùng một ngân hàng question tổng hợp.
- Lịch sử: từ `1 + số quiz + số subject` xuống 1 request vì backend đã trả metadata.
- Import question: dữ liệu subject/chapter được tải bằng 2 request thay cho `1 + số môn`.
- `apiClient` gộp các GET giống nhau đang cùng in-flight theo URL, params, access token và preview mode. Mutation không cache nên dữ liệu ghi không bị thay đổi hành vi.

## 5. Độ phức tạp query trước và sau

| Luồng | Trước | Sau |
|---|---:|---:|
| Tải Q câu trong quiz | `O(Q)`; khoảng `4 + 3Q + số skill` | khoảng 7 query cố định |
| Danh sách question của chapter | `O(Q)` | khoảng 5 query cố định |
| Lịch sử A attempts | xấp xỉ `1 + 4A` | 4 query batch chính |
| Danh sách U users và role | `1 + 2U` | 3 query |
| Submit có S skill | đọc/ghi tăng khoảng `3S` | một ability read + hai batch write |
| Adaptive trên C chapter | question và chapter-skill tăng theo C | hai batch query |
| Dashboard frontend | tăng theo subject/chapter | 1 HTTP request, 1 aggregate SQL |

Các con số có thể cộng thêm query kiểm tra visibility tùy role, nhưng không còn tăng theo số question/attempt/user.

## 6. Đo runtime với PostgreSQL VPS

Ứng dụng được chạy thật bằng Spring Boot và gọi tới PostgreSQL `34.87.73.15`. Tất cả endpoint dưới đây trả HTTP 200:

| Endpoint | Warm latency quan sát |
|---|---:|
| `/api/subjects` | 107–110 ms |
| `/api/chapters` | 270–275 ms |
| `/api/subjects/{id}/chapters` | 263–271 ms |
| `/api/subjects/{id}/theories` | 342–348 ms |
| `/api/subjects/{id}/quizzes` | 210–218 ms |
| `/api/chapters/{id}/questions` | 421–429 ms |
| `/api/quizzes/{id}/questions` | 509–516 ms |

Latency còn lại chủ yếu là số round-trip cố định tới database ở xa khoảng 55 ms. Quan trọng hơn, latency sau refactor bị chặn theo số query cố định: thêm question/chapter/attempt không còn làm thời gian tăng tuyến tính. Để giảm tiếp phần cố định, backend production nên đặt cùng region/private network với PostgreSQL.

Baseline code cũ không được checkout/chạy lại trên database đang dùng để tránh đảo migration và thay đổi worktree. Theo query-count tĩnh, quiz 20 câu trước đây có thể vượt 65 DB round-trip, tương đương tối thiểu khoảng 3,5 giây chỉ riêng RTT; sau refactor số query của endpoint này là hằng số.

## 7. Verification

- Backend: toàn bộ Maven test suite chạy thành công, bao gồm test nghiệp vụ normal/adaptive quiz.
- Có test regression xác nhận quiz 40 câu dùng đúng một bulk query và không gọi `findByQuestionId` từng câu.
- Có integration test chạy aggregate stats SQL trực tiếp trên PostgreSQL được cấu hình.
- Spring context khởi động thành công và Flyway validate đủ 6 migrations.
- Frontend ESLint thành công.
- Frontend production build bằng Vite thành công.
- 41 frontend unit tests thành công.
- Smoke test runtime các endpoint public đều HTTP 200.

## 8. Cảnh báo không chặn release

- Flyway hiện cảnh báo PostgreSQL 18.4 mới hơn version được Flyway chính thức kiểm thử (17); migration vẫn validate và chạy thành công.
- Vite cảnh báo một số chunk lớn hơn 500 kB. Đây là vấn đề tải asset, không phải data-query latency và không thuộc thay đổi nghiệp vụ lần này.
- Import question vẫn gửi từng item tuần tự để giữ nguyên hành vi progress và khả năng báo lỗi/giữ các item đã thành công. Việc đổi sang all-or-nothing bulk import sẽ làm thay đổi nghiệp vụ nên không được thực hiện.
