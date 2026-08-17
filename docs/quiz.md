# 📝 Quiz & Adaptive Learning API Specification

Tài liệu chi tiết về các API liên quan đến làm bài kiểm tra (`Quiz`, `Attempt`) và học tập thích ứng (`Adaptive Learning`), tổng hợp từ `AttemptController`, `AdaptiveQuizController`, `QuizAdminController` và các Service nghiệp vụ.

---

## 1. 🌐 Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| **Base URL** | `http://<host>/api` |
| **Content-Type** | `application/json` |
| **Authentication** | `Authorization: Bearer <accessToken>` |
| **Response chuẩn** | `ApiResponse<T>` với cấu trúc `{ status, message, data }` |

---

## 2. 🧠 Học tập thích ứng (Adaptive Learning)

Hệ thống học tập thích ứng (Adaptive Quiz) tự động phân tích năng lực học viên qua các kỹ năng (IRT theta) và chọn các câu hỏi phù hợp nhất theo từng nhóm 5 câu (Batch size = 5).

### 2.1 Quy tắc giới hạn & Điều kiện làm bài thích ứng
- **Điều kiện mở khóa:** Học viên phải hoàn thành **tối thiểu 2 bài kiểm tra tiêu chuẩn** (loại `CHAPTER` hoặc `FINAL`) trong môn học trước khi có thể bắt đầu hoặc tiếp tục làm bài thích ứng mới.
- **Cơ chế Tracking (`user_adaptive_quiz_trackings`):**
  - Khi học viên nộp và hoàn thành một bài quiz `CHAPTER` hoặc `FINAL`, hệ thống tự động cộng dồn số bài kiểm tra tiêu chuẩn đã hoàn thành (`completed_standard_quizzes += 1`).
  - Khi học viên hoàn thành bài kiểm tra thích ứng (nộp hết tất cả các câu), bộ đếm số bài tiêu chuẩn sẽ được reset về `0`. Học viên cần làm thêm 2 bài kiểm tra tiêu chuẩn khác để mở lượt làm thích ứng tiếp theo.
  - Quản trị viên (`ADMINISTRATOR`) có quyền làm bài thích ứng mà không bị giới hạn bởi bộ đếm này.

---

### 2.2 Kiểm tra điều kiện làm bài thích ứng

Kiểm tra số bài quiz tiêu chuẩn đã làm và số bài còn lại cần làm để mở khóa bài kiểm tra thích ứng của một môn học (phục vụ frontend hiển thị trạng thái và tiến độ cho người dùng).

```http
GET /api/subjects/{subjectId}/adaptive-eligibility
```
*(Hoặc alias: `GET /api/subjects/{subjectId}/adaptive-attempts/eligibility`)*

**Quyền:** `USER` (đã đăng nhập và đã đăng ký môn học) hoặc `ADMINISTRATOR`.

#### Response — `200 OK` (Chưa đủ điều kiện):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "subjectId": "sub-123",
    "requiredQuizzes": 2,
    "completedQuizzes": 1,
    "remainingQuizzes": 1,
    "eligible": false
  }
}
```

#### Response — `200 OK` (Đã đủ điều kiện):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "subjectId": "sub-123",
    "requiredQuizzes": 2,
    "completedQuizzes": 2,
    "remainingQuizzes": 0,
    "eligible": true
  }
}
```

---

### 2.3 Bắt đầu làm bài thích ứng

Khởi tạo một lượt làm bài thích ứng cho môn học. Hệ thống sẽ kiểm tra điều kiện (tối thiểu 2 bài quiz thường đã hoàn thành) và chuẩn bị sẵn 10 câu hỏi đầu tiên (buffer), nhưng chỉ trả về 5 câu đầu tiên cho đợt làm đầu.

```http
POST /api/subjects/{subjectId}/adaptive-attempts
```

**Quyền:** `USER` (đã đăng nhập, đủ điều kiện) hoặc `ADMINISTRATOR`.

#### Request Body:
```json
{
  "totalQuestion": 15
}
```
*Ghi chú:* `totalQuestion` phải là bội số của 5 và nằm trong khoảng từ `15` đến `75` (ví dụ: 15, 20, 25, 30...).

#### Response — `201 Created`:
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "attemptId": "attempt-uuid",
    "quizId": "quiz-uuid",
    "totalQuestion": 15,
    "currentBatch": 1,
    "totalBatch": 3,
    "completed": false,
    "questions": [
      {
        "questionId": "q-1",
        "content": "Nội dung câu hỏi...",
        "contentFormat": "plain",
        "difficultyB": 0.5,
        "options": [
          { "optionId": "opt-1", "content": "Đáp án A" },
          { "optionId": "opt-2", "content": "Đáp án B" }
        ],
        "skills": [
          { "skillId": "skill-1", "skillName": "Vòng lặp", "primary": true, "weight": 1.0 }
        ]
      }
    ]
  }
}
```

#### Response lỗi khi chưa đủ điều kiện (`403 Forbidden`):
```json
{
  "status": 403,
  "message": "Bạn cần hoàn thành tối thiểu 2 bài kiểm tra khác (Chương hoặc Tổng kết) trước khi làm bài thích ứng. Còn lại: 2 bài.",
  "data": null
}
```

---

### 2.4 Lấy trạng thái đợt câu hỏi hiện tại

Lấy lại danh sách 5 câu hỏi của đợt làm bài thích ứng hiện tại nếu người dùng tải lại trang.

```http
GET /api/adaptive-attempts/{attemptId}
```

**Quyền:** `USER` sở hữu attempt hoặc `ADMINISTRATOR`.

**Response — `200 OK`:** Trả về `AdaptiveQuizResponse` chứa 5 câu hỏi chưa hoàn thành của đợt hiện tại.

---

### 2.5 Nộp câu trả lời theo đợt (Batch Submit)

Nộp câu trả lời cho đúng 5 câu hỏi của đợt hiện tại. Hệ thống cập nhật theta năng lực của học viên ngay lập tức, sinh tiếp 5 câu tiếp theo hoặc hoàn thành bài thi nếu đã đến đợt cuối.

```http
POST /api/adaptive-attempts/{attemptId}/answers
```

**Quyền:** `USER` sở hữu attempt.

#### Request Body:
```json
{
  "answers": [
    { "questionId": "q-1", "optionId": "opt-1" },
    { "questionId": "q-2", "optionId": "opt-4" },
    { "questionId": "q-3", "optionId": "opt-2" },
    { "questionId": "q-4", "optionId": "opt-3" },
    { "questionId": "q-5", "optionId": "opt-1" }
  ]
}
```

#### Response — `200 OK` (Khi chuyển sang đợt kế tiếp):
`data.completed = false`, `data.currentBatch` tăng lên và `data.questions` chứa 5 câu hỏi của đợt tiếp theo.

#### Response — `200 OK` (Khi nộp xong đợt cuối cùng):
`data.completed = true`, `data.questions` rỗng. Đồng thời:
- Lời khuyên học tập (AI advice) được sinh tự động và lưu vào attempt.
- Bộ đếm `completed_standard_quizzes` của user trong môn học được tự động reset về `0`.

---

## 3. 📋 Làm bài kiểm tra tiêu chuẩn (`CHAPTER` & `FINAL`)

### 3.1 Bắt đầu làm bài kiểm tra
```http
POST /api/quizzes/{quizId}/attempts
```
**Quyền:** `USER` đã đăng ký môn học và mở khóa chương (nếu là quiz chương).

**Response — `200 OK`:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "attemptId": "attempt-uuid",
    "quizId": "quiz-uuid",
    "totalQuestions": 20,
    "questions": [ ... ]
  }
}
```

---

### 3.2 Nộp bài kiểm tra tiêu chuẩn
```http
POST /api/attempts/submit
```
**Quyền:** `USER` sở hữu attempt.

#### Request Body:
```json
{
  "attemptId": "attempt-uuid",
  "quizId": "quiz-uuid",
  "answers": [
    { "questionId": "q-1", "optionId": "opt-1" }
  ]
}
```

#### Hành vi hệ thống khi nộp thành công:
1. Chấm điểm và tính toán cập nhật năng lực học viên theo từng kỹ năng (IRT theta & blended theta).
2. Sinh lời khuyên học tập từ AI dựa trên sự thay đổi kỹ năng sau bài làm.
3. **Cập nhật tracking làm bài thích ứng:** Tự động tăng bộ đếm số bài tiêu chuẩn đã làm (`completed_standard_quizzes += 1`) cho môn học tương ứng trong bảng `user_adaptive_quiz_trackings`.

**Response — `200 OK`:** Trả về kết quả chi tiết `QuizResultResponse` kèm lời khuyên học tập và phân tích kỹ năng.

---

### 3.3 Xem kết quả bài kiểm tra
```http
GET /api/attempts/{attemptId}/result
```
**Quyền:** `USER` sở hữu attempt hoặc `ADMINISTRATOR`.

**Response — `200 OK`:** `ApiResponse<QuizResultResponse>` bao gồm điểm số, đáp án đúng/sai, giải thích chi tiết câu hỏi, năng lực kỹ năng và lời khuyên ôn tập.
