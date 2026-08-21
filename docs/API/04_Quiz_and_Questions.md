# 📝 Module 4: Ngân hàng Câu hỏi & Đề thi (Quiz & Questions API Specification)

Tài liệu chi tiết các endpoint Quản lý Ngân hàng Câu hỏi (`/api/questions`), Gán vị trí môn học/chương, Nhập câu hỏi hàng loạt và Quản lý Đề thi Tiêu chuẩn (`/api/quizzes`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `GET` | `/api/questions` | `ADMINISTRATOR` | Lấy toàn bộ danh sách câu hỏi trong ngân hàng. |
| `GET` | `/api/chapters/{chapterId}/questions` | `isAuthenticated()` | Lấy danh sách câu hỏi thuộc về một chương học cụ thể. |
| `POST` | `/api/questions` | `ADMINISTRATOR` | Tạo mới một câu hỏi kèm các đáp án và kỹ năng liên kết. |
| `POST` | `/api/questions/bulk` | `ADMINISTRATOR` | Tạo hoặc import hàng loạt câu hỏi cùng lúc. |
| `GET` | `/api/questions/{questionId}` | Public / User / Admin | Lấy chi tiết câu hỏi (Ẩn đáp án đúng nếu là học viên). |
| `PUT` | `/api/questions/{questionId}` | `ADMINISTRATOR` | Cập nhật nội dung câu hỏi, tham số IRT, đáp án, kỹ năng. |
| `DELETE` | `/api/questions/{questionId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) câu hỏi. |
| `PATCH` | `/api/questions/{questionId}/placement` | `ADMINISTRATOR` | Gán hoặc chuyển đổi môn học và chương cho một câu hỏi. |
| `PATCH` | `/api/questions/placement` | `ADMINISTRATOR` | Gán môn học và chương hàng loạt cho nhiều câu hỏi. |
| `GET` | `/api/chapters/{chapterId}/quizzes` | `isAuthenticated()` | Lấy danh sách đề thi thuộc về một chương học. |
| `POST` | `/api/quizzes` | `ADMINISTRATOR` | Tạo mới một đề thi tiêu chuẩn (`CHAPTER` hoặc `FINAL`). |
| `GET` | `/api/quizzes/{quizId}` | `isAuthenticated()` | Lấy thông tin chi tiết đề thi (thời gian làm bài, số lượng câu). |
| `GET` | `/api/quizzes/{quizId}/questions` | `isAuthenticated()` | Lấy danh sách toàn bộ câu hỏi và lựa chọn để học viên làm bài. |
| `PUT` | `/api/quizzes/{quizId}` | `ADMINISTRATOR` | Cập nhật thông tin đề thi và cấu trúc danh sách câu hỏi. |
| `PUT` | `/api/chapters/{chapterId}/quizzes/display-order` | `ADMINISTRATOR` | Cập nhật thứ tự hiển thị các đề thi trong chương. |
| `DELETE` | `/api/quizzes/{quizId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) đề thi (có kiểm tra an toàn). |

---

## 2. ❓ Chi tiết Quản lý Ngân hàng Câu hỏi (`/api/questions`)

### 2.1 Cấu trúc Tham số IRT của Câu hỏi
Mỗi câu hỏi trắc nghiệm trong hệ thống QuizCit được mô hình hóa theo lý thuyết IRT 3-tham số (3PL):
- `difficulty` ($b$): Độ khó của câu hỏi, thang đo chuẩn hóa thường từ `-3.0` (rất dễ) đến `+3.0` (rất khó). Mặc định: `0.0`.
- `discrimination` ($a$): Độ phân biệt của câu hỏi ($a > 0$, mặc định `1.0`).
- `guessing` ($c$): Xác suất đoán mò trúng ($0 \le c < 1$, mặc định `0.25` cho trắc nghiệm 4 lựa chọn).

---

### 2.2 Tạo Câu hỏi mới (`POST /api/questions`)
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "subjectId": "sub_ct101",
  "chapterId": "chp_pointer",
  "content": "Đoạn mã sau in ra giá trị gì?\n```c\nint a = 10;\nint *p = &a;\nprintf(\"%d\", *p + 5);\n```",
  "contentFormat": "MARKDOWN",
  "questionType": "SINGLE_CHOICE",
  "difficulty": 0.5,
  "discrimination": 1.2,
  "guessing": 0.25,
  "explanation": "Toán tử *p truy xuất giá trị ô nhớ mà p trỏ tới (a = 10). Phép cộng 10 + 5 cho kết quả 15.",
  "options": [
    {
      "content": "15",
      "contentFormat": "PLAIN_TEXT",
      "isCorrect": true,
      "order": 1
    },
    {
      "content": "10",
      "contentFormat": "PLAIN_TEXT",
      "isCorrect": false,
      "order": 2
    },
    {
      "content": "Địa chỉ ô nhớ của a",
      "contentFormat": "PLAIN_TEXT",
      "isCorrect": false,
      "order": 3
    },
    {
      "content": "Lỗi biên dịch",
      "contentFormat": "PLAIN_TEXT",
      "isCorrect": false,
      "order": 4
    }
  ],
  "skills": [
    {
      "skillId": "skl_pointer_basic",
      "weight": 1.0,
      "isPrimary": true
    }
  ]
}
```

#### Response `201 Created`:
```json
{
  "status": 201,
  "message": "Tạo câu hỏi thành công",
  "data": {
    "questionId": "qst_pointer_001",
    "subjectId": "sub_ct101",
    "chapterId": "chp_pointer",
    "content": "Đoạn mã sau in ra giá trị gì?...",
    "difficulty": 0.5,
    "options": [ ... ],
    "skills": [ ... ]
  }
}
```

---

### 2.3 Nhập câu hỏi hàng loạt (`POST /api/questions/bulk`)
- **Usecase:** Nhập hàng loạt câu hỏi từ các định dạng Moodle XML, GIFT, Aiken hoặc CSV.
- **Quyền:** `ADMINISTRATOR`
- **Request Body:** `{ "items": [ { "clientId": "row-1", "question": { ... } }, ... ] }`
- **Response `200 OK`:** `BulkCreateQuestionsResponse` (báo cáo chi tiết số lượng câu thành công, thất bại và ID tương ứng).

---

### 2.4 Gán vị trí môn học và chương hàng loạt (`PATCH /api/questions/placement`)
- **Usecase:** Chuyển nhanh một nhóm câu hỏi từ Ngân hàng chung vào một Môn học và Chương cụ thể.
- **Request Body:**
```json
{
  "questionIds": [
    "qst_001",
    "qst_002",
    "qst_003"
  ],
  "subjectId": "sub_ct101",
  "chapterId": "chp_pointer"
}
```

---

### 2.5 Xóa Câu hỏi (`DELETE /api/questions/{questionId}`)
- **Query Params:**
  - `hard`: `false` (mặc định - **Xóa mềm**):
    - Đánh dấu `deleted_at`. Câu hỏi sẽ không xuất hiện trong các bài thi mới.
    - **Quan trọng:** Không xóa các câu trả lời (`responses`) trong quá khứ, giữ nguyên vẹn điểm số và lịch sử bài thi của các học viên đã từng làm câu hỏi này.
  - `hard`: `true` (**Xóa cứng**):
    - Xóa vĩnh viễn khỏi cơ sở dữ liệu và dọn dẹp các bảng phụ thuộc (`options`, `question_skills`).
- **Response `204 No Content`**.

---

## 3. 🎯 Chi tiết Quản lý Đề thi (`/api/quizzes`)

### 3.1 Tạo mới Đề thi (`POST /api/quizzes`)
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "chapterId": "chp_pointer",
  "title": "Bài kiểm tra Đánh giá Chương 3: Con trỏ",
  "description": "Thời gian 20 phút - 15 câu hỏi trắc nghiệm",
  "type": "CHAPTER",
  "timeLimit": 1200,
  "totalQuestion": 15,
  "passPercentage": 60,
  "questionIds": [
    "qst_pointer_001",
    "qst_pointer_002",
    "qst_pointer_003"
  ]
}
```
*Ghi chú:*
- `type`: Loại đề thi (`CHAPTER`: Đề thi hết chương; `FINAL`: Đề thi tổng kết môn học).
- `timeLimit`: Thời gian làm bài tính bằng giây (Ví dụ: `1200` = 20 phút). `0` nghĩa là không giới hạn.
- `passPercentage`: Tỷ lệ % điểm số cần đạt để tính là Đạt (Ví dụ: `60`%).

---

### 3.2 Lấy danh sách câu hỏi trong Đề thi để làm bài (`GET /api/quizzes/{quizId}/questions`)
- **Usecase:** Frontend gọi API này khi học viên chuẩn bị làm bài để render danh sách câu hỏi và các lựa chọn đáp án.
- **Bảo mật:** Backend tự động ẩn cờ `isCorrect` và phần `explanation` (giải thích) đối với tài khoản học viên để chống gian lận.
- **Response `200 OK`:** Danh sách `QuestionResponse` kèm các `options` (đã xáo trộn hoặc theo thứ tự bài thi).

---

### 3.3 Xóa Đề thi (`DELETE /api/quizzes/{quizId}`)
- **Query Params:**
  - `hard`: `false` (mặc định - **Xóa mềm**): Đánh dấu ẩn đề thi khỏi danh sách môn học.
  - `hard`: `true` (**Xóa cứng**): Yêu cầu xóa vĩnh viễn.

#### ⚠️ Quy tắc Kiểm tra An toàn khi Xóa cứng:
- Backend thực hiện kiểm tra `existsAttemptByQuizId(quizId)`:
  - **Nếu đề thi ĐÃ CÓ người từng làm bài thi:** Backend **chặn thao tác xóa cứng** và trả về mã lỗi:
    - **HTTP Status `400 Bad Request`**:
      ```json
      {
        "status": 400,
        "message": "Đề thi đã có lượt làm bài, không thể xóa vĩnh viễn. Vui lòng sử dụng tính năng xóa mềm (ẩn đề thi).",
        "data": null
      }
      ```
  - **Nếu đề thi CHƯA CÓ ai làm:** Cho phép xóa cứng vĩnh viễn và tự động xóa các liên kết trong `quiz_questions`.
