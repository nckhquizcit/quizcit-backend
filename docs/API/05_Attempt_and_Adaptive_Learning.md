# 🎓 Module 5: Làm bài Kiểm tra & Học thích ứng (Attempt & Adaptive Learning API Specification)

Tài liệu chi tiết các endpoint phục vụ Học viên Làm bài kiểm tra Tiêu chuẩn (`/api/quizzes/{id}/start`, `/api/attempts`), Xem kết quả chi tiết kèm Phân tích Năng lực IRT và Quy trình Làm bài kiểm tra Thích ứng (CAT - Computerized Adaptive Testing) theo lô câu hỏi (`/api/adaptive-attempts`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `POST` | `/api/quizzes/{quizId}/start` | `isAuthenticated()` | Bắt đầu làm một bài kiểm tra tiêu chuẩn (tạo mới `Attempt`). |
| `POST` | `/api/attempts/submit` | Owner / `ADMINISTRATOR` | Nộp bài thi tiêu chuẩn, chấm điểm và tính toán điểm năng lực IRT ($\theta$). |
| `POST` | `/api/quizzes/{quizId}/attempts/{attemptId}/submit` | Owner / `ADMINISTRATOR` | Endpoint thay thế để nộp bài thi cho một lượt thi cụ thể. |
| `GET` | `/api/attempts/{attemptId}/result` | Owner / `ADMINISTRATOR` | Lấy kết quả chi tiết, bảng điểm từng câu, biến thiên IRT và lời khuyên AI. |
| `GET` | `/api/users/{userId}/attempts` | Owner / `ADMINISTRATOR` | Lấy danh sách lịch sử tất cả các lượt thi của học viên. |
| `GET` | `/api/subjects/{subjectId}/adaptive-eligibility` | `isAuthenticated()` | Kiểm tra điều kiện mở khóa bài kiểm tra thích ứng (cần làm $\ge 2$ bài tiêu chuẩn). |
| `POST` | `/api/subjects/{subjectId}/adaptive-attempts` | `isAuthenticated()` | Bắt đầu phiên làm bài thi thích ứng (nhận lô 5 câu hỏi đầu tiên). |
| `GET` | `/api/adaptive-attempts/{attemptId}` | Owner / `ADMINISTRATOR` | Lấy trạng thái hiện tại của lượt làm bài thích ứng đang diễn ra. |
| `POST` | `/api/adaptive-attempts/{attemptId}/answers` | Owner / `ADMINISTRATOR` | Nộp câu trả lời cho lô 5 câu hiện tại, ước lượng lại $\theta$ và nhận lô 5 câu tiếp theo. |

---

## 2. 📝 Chi tiết Quy trình Làm bài Kiểm tra Tiêu chuẩn

### 2.1 Bắt đầu làm bài thi (`POST /api/quizzes/{quizId}/start`)
- **Usecase:** Học viên nhấn "Bắt đầu làm bài" trên trang chi tiết đề thi. Hệ thống tạo một bản ghi `attempts` mới với `startedAt = CURRENT_TIMESTAMP`.
- **Quyền:** `isAuthenticated()`.

#### Response `201 Created`:
```json
{
  "status": 201,
  "message": "Success",
  "data": {
    "attemptId": "att_20260821_001",
    "quizId": "qiz_pointer_test",
    "quizTitle": "Bài kiểm tra Chương 3: Con trỏ",
    "timeLimit": 1200,
    "totalQuestions": 15,
    "startedAt": "2026-08-21T10:00:00Z",
    "questions": [
      {
        "questionId": "qst_001",
        "content": "Toán tử nào dùng để lấy địa chỉ của một biến trong C?",
        "contentFormat": "PLAIN_TEXT",
        "options": [
          { "optionId": "opt_1", "content": "&", "order": 1 },
          { "optionId": "opt_2", "content": "*", "order": 2 },
          { "optionId": "opt_3", "content": "->", "order": 3 },
          { "optionId": "opt_4", "content": "%", "order": 4 }
        ]
      }
    ]
  }
}
```

---

### 2.2 Nộp bài thi (`POST /api/attempts/submit`)
- **Usecase:** Học viên nộp bài khi hết giờ hoặc khi hoàn thành các câu hỏi. Hệ thống tự động so khớp đáp án đúng, tính điểm số, lưu `responses`, gọi giải thuật IRT để cập nhật $\theta$ của từng kỹ năng liên quan và tăng bộ đếm bài tiêu chuẩn (`completed_standard_quizzes += 1`).
- **Quyền:** Chủ sở hữu lượt thi hoặc `ADMINISTRATOR`.

#### Request Body:
```json
{
  "attemptId": "att_20260821_001",
  "quizId": "qiz_pointer_test",
  "answers": [
    {
      "questionId": "qst_001",
      "selectedOptionId": "opt_1"
    },
    {
      "questionId": "qst_002",
      "selectedOptionId": "opt_3"
    }
  ]
}
```

#### Response `200 OK` (`QuizResultResponse`):
```json
{
  "status": 200,
  "message": "Nộp bài kiểm tra thành công",
  "data": {
    "attemptId": "att_20260821_001",
    "quizId": "qiz_pointer_test",
    "quizTitle": "Bài kiểm tra Chương 3: Con trỏ",
    "score": 8.0,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "passed": true,
    "submittedAt": "2026-08-21T10:18:30Z",
    "skillResults": [
      {
        "skillId": "skl_pointer_basic",
        "skillName": "Toán tử Con trỏ",
        "oldTheta": 0.20,
        "newTheta": 0.85,
        "thetaChange": 0.65
      }
    ],
    "questionResults": [
      {
        "questionId": "qst_001",
        "content": "Toán tử nào dùng để lấy địa chỉ...",
        "selectedOptionId": "opt_1",
        "correctOptionId": "opt_1",
        "isCorrect": true,
        "explanation": "Toán tử & (address-of) lấy địa chỉ ô nhớ."
      }
    ],
    "overallAdvice": "Bạn nắm rất vững phần con trỏ cơ bản. Hãy tiếp tục luyện tập thêm các câu hỏi về con trỏ cấp 2 và cấp phát động."
  }
}
```

---

### 2.3 Xem kết quả chi tiết của lượt thi (`GET /api/attempts/{attemptId}/result`)
- **Usecase:** Học viên xem lại lịch sử bài thi, xem đáp án đúng và lời giải chi tiết sau khi bài thi đã kết thúc.
- **Quyền:** Chủ sở hữu lượt thi hoặc `ADMINISTRATOR`.

---

## 3. 🧠 Chi tiết Cơ chế Học tập Thích ứng (CAT - Computerized Adaptive Testing)

Hệ thống CAT tự động phân tích điểm năng lực $\theta$ hiện tại của học viên để lựa chọn câu hỏi có **Độ thông tin Fisher tối đa (Maximum Information Item Selection)**, giúp đánh giá chính xác năng lực trong thời gian ngắn nhất.

### 3.1 Quy tắc nghiệp vụ & Giới hạn
1. **Điều kiện mở khóa:** Học viên cần hoàn thành tối thiểu **2 bài kiểm tra tiêu chuẩn** trong môn học trước khi làm bài thích ứng (`completed_standard_quizzes >= 2`).
2. **Cơ chế phân lô (Batching):** Câu hỏi được đưa ra theo từng nhóm **5 câu** (Batch Size = 5). Tổng số câu hỏi của một bài thích ứng là bội số của 5 (Ví dụ: 15, 20 hoặc 25 câu).
3. **Cập nhật $\theta$ tức thời (Real-time Adaptive Update):** Sau mỗi lô 5 câu, hệ thống tính toán lại điểm năng lực $\theta$ và chọn 5 câu tiếp theo có độ khó bám sát với $\theta$ mới.
4. **Reset bộ đếm:** Khi hoàn thành bài thi thích ứng, bộ đếm `completed_standard_quizzes` tự động đặt về `0`. Học viên cần làm thêm 2 bài tiêu chuẩn để tiếp tục mở lượt thích ứng mới. Quản trị viên (`ADMINISTRATOR`) không bị ràng buộc bởi điều kiện này.

---

### 3.2 Kiểm tra điều kiện mở khóa bài thích ứng (`GET /api/subjects/{subjectId}/adaptive-eligibility`)

#### Response `200 OK` (Chưa đủ điều kiện):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "subjectId": "sub_ct101",
    "requiredQuizzes": 2,
    "completedQuizzes": 1,
    "remainingQuizzes": 1,
    "eligible": false
  }
}
```

#### Response `200 OK` (Đã đủ điều kiện):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "subjectId": "sub_ct101",
    "requiredQuizzes": 2,
    "completedQuizzes": 2,
    "remainingQuizzes": 0,
    "eligible": true
  }
}
```

---

### 3.3 Bắt đầu làm bài thi thích ứng (`POST /api/subjects/{subjectId}/adaptive-attempts`)
- **Request Body:**
```json
{
  "totalQuestion": 15
}
```
*Ghi chú:* `totalQuestion` phải $\ge 15$ và chia hết cho 5 (Ví dụ: `15`, `20`, `25`).

#### Response `201 Created`:
```json
{
  "status": 201,
  "message": "Success",
  "data": {
    "attemptId": "att_adapt_999",
    "subjectId": "sub_ct101",
    "currentBatch": 1,
    "totalBatches": 3,
    "totalQuestions": 15,
    "completedQuestions": 0,
    "currentTheta": 0.45,
    "isCompleted": false,
    "questions": [
      { "questionId": "qst_ad_1", "content": "Câu hỏi thích ứng số 1...", "options": [ ... ] },
      { "questionId": "qst_ad_2", "content": "Câu hỏi thích ứng số 2...", "options": [ ... ] },
      { "questionId": "qst_ad_3", "content": "Câu hỏi thích ứng số 3...", "options": [ ... ] },
      { "questionId": "qst_ad_4", "content": "Câu hỏi thích ứng số 4...", "options": [ ... ] },
      { "questionId": "qst_ad_5", "content": "Câu hỏi thích ứng số 5...", "options": [ ... ] }
    ]
  }
}
```

#### Lỗi có thể gặp:
- `400 Bad Request`: `Bạn cần hoàn thành tối thiểu 2 bài kiểm tra tiêu chuẩn trước khi làm bài thích ứng.`

---

### 3.4 Nộp câu trả lời theo lô 5 câu (`POST /api/adaptive-attempts/{attemptId}/answers`)
- **Usecase:** Học viên làm xong 5 câu hiện tại và bấm "Tiếp tục" để nhận 5 câu tiếp theo hoặc kết thúc bài thi.

#### Request Body:
```json
{
  "answers": [
    { "questionId": "qst_ad_1", "selectedOptionId": "opt_a" },
    { "questionId": "qst_ad_2", "selectedOptionId": "opt_b" },
    { "questionId": "qst_ad_3", "selectedOptionId": "opt_c" },
    { "questionId": "qst_ad_4", "selectedOptionId": "opt_d" },
    { "questionId": "qst_ad_5", "selectedOptionId": "opt_a" }
  ]
}
```

#### Response `200 OK` (Chuyển sang lô tiếp theo):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "attemptId": "att_adapt_999",
    "currentBatch": 2,
    "totalBatches": 3,
    "totalQuestions": 15,
    "completedQuestions": 5,
    "currentTheta": 1.15,
    "isCompleted": false,
    "questions": [
      { "questionId": "qst_ad_6", "content": "Câu hỏi thích ứng số 6 độ khó cao hơn...", "options": [ ... ] },
      { "questionId": "qst_ad_7", "content": "Câu hỏi thích ứng số 7...", "options": [ ... ] },
      { "questionId": "qst_ad_8", "content": "Câu hỏi thích ứng số 8...", "options": [ ... ] },
      { "questionId": "qst_ad_9", "content": "Câu hỏi thích ứng số 9...", "options": [ ... ] },
      { "questionId": "qst_ad_10", "content": "Câu hỏi thích ứng số 10...", "options": [ ... ] }
    ]
  }
}
```

#### Response `200 OK` (Khi hoàn thành lô cuối cùng):
- `isCompleted: true`
- `questions: []`
- `finalTheta`: Điểm năng lực hội tụ cuối cùng của học viên.
- Hệ thống tự động lưu kết quả vào `user_skill_ability` và `ability_history`.
