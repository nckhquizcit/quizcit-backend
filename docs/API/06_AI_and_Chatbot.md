# 🤖 Module 6: Trợ lý AI & Chatbot RAG (AI & Chatbot API Specification)

Tài liệu chi tiết các endpoint Ước lượng Năng lực IRT bằng AI Service (`/api/ai/estimate`) và Trợ lý Gia sư AI Chatbot hỏi đáp thông minh tích hợp RAG truy xuất ngữ cảnh lý thuyết môn học (`/api/chatbot/ask`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `POST` | `/api/ai/estimate` | `isAuthenticated()` | Ước lượng điểm năng lực $\theta$ (Theta) từ vector câu trả lời và trọng số kỹ năng. |
| `POST` | `/api/chatbot/ask` | `isAuthenticated()` | Hỏi đáp với Trợ lý AI Chatbot dựa trên ngữ cảnh các chương lý thuyết đã mở khóa (RAG). |

---

## 2. 🧮 Chi tiết Dịch vụ Ước lượng Năng lực IRT (`/api/ai/estimate`)

### 2.1 Tổng quan
- **Mục đích:** Cung cấp API trực tiếp cho các dịch vụ chấm điểm hoặc mô phỏng tính toán năng lực học viên $\theta$ dựa trên mô hình Lý thuyết Ứng đáp Câu hỏi (Item Response Theory).
- **Quy trình hoạt động:** Backend Spring Boot chuyển tiếp dữ liệu làm bài sang AI/Python Service (hoặc sử dụng thuật toán nội bộ EAP / Newton-Raphson MLE Fallback) để tính toán $\theta$ mới và độ lệch chuẩn (Standard Error).
- **Quyền:** `isAuthenticated()`.

### 2.2 Request Body:
```json
{
  "responses": [1, 1, 0, 1, 0],
  "difficulties": [-0.5, 0.2, 1.1, 0.8, 1.5],
  "weights": [1.0, 1.0, 1.0, 1.0, 1.0]
}
```
*Ghi chú:*
- `responses`: Danh sách giá trị nhị phân `1` (đúng) hoặc `0` (sai) tương ứng từng câu hỏi.
- `difficulties`: Tham số độ khó ($b$) tương ứng của từng câu hỏi.
- `weights`: Trọng số đóng góp của câu hỏi đối với kỹ năng đang xét (`question_skills.weight`).

### 2.3 Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "theta": 0.68,
    "standardError": 0.32
  }
}
```

### 2.4 Các lỗi có thể gặp:
- `400 Bad Request`: `Danh sách responses, difficulties và weights không được để trống và phải có độ dài bằng nhau.`

---

## 3. 💬 Chi tiết Trợ lý Gia sư AI Chatbot (`/api/chatbot/ask`)

### 3.1 Cơ chế Hoạt động RAG (Retrieval-Augmented Generation)
1. **Kiểm tra phạm vi kiến thức (Knowledge Boundary):** Hệ thống xác định danh sách các chương học mà sinh viên đã mở khóa (`user_unlocked_chapters`) trong môn học `subjectId`. Sinh viên chỉ được hỏi và nhận câu trả lời từ các chương kiến thức đã học để đảm bảo tính sư phạm.
2. **Truy vấn Ngữ nghĩa (Vector Semantic Search):** Câu hỏi của sinh viên được sinh vector embedding và so khớp độ tương đồng Cosine (Cosine Similarity) với các đoạn lý thuyết trong cơ sở dữ liệu `theory_chunks` (PostgreSQL `pgvector`).
3. **Tổng hợp câu trả lời:** AI Model tổng hợp thông tin trích xuất từ các bài học liên quan và giải thích dễ hiểu, kèm trích dẫn nguồn bài học (`sources`).

---

### 3.2 Gửi câu hỏi cho Chatbot (`POST /api/chatbot/ask`)
- **Quyền:** `isAuthenticated()`.

#### Request Body:
```json
{
  "subjectId": "sub_ct101",
  "chapterId": "chp_pointer",
  "message": "Tại sao sau khi dùng hàm malloc() trong C thì bắt buộc phải gọi free()?",
  "history": [
    {
      "role": "user",
      "content": "Con trỏ có thể trỏ vào đâu?"
    },
    {
      "role": "assistant",
      "content": "Con trỏ trong C có thể trỏ tới địa chỉ của một biến thông thường hoặc vùng nhớ được cấp phát động trong Heap..."
    }
  ]
}
```
*Ghi chú:*
- `subjectId`: Bắt buộc. Môn học sinh viên đang tương tác.
- `chapterId`: Không bắt buộc. Chương bài học hiện tại (dùng để ưu tiên ngữ cảnh gần nhất nếu có).
- `message`: Bắt buộc. Câu hỏi hoặc thắc mắc của sinh viên.
- `history`: Danh sách các lượt trò chuyện gần nhất trong phiên hỏi đáp (Frontend lưu giữ state và gửi kèm).

#### Response `200 OK` (`ChatbotAnswerResponse`):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "answer": "Khi bạn sử dụng hàm `malloc()`, hệ điều hành sẽ cấp phát một vùng nhớ trong bộ nhớ Heap. Bộ nhớ Heap không tự động giải phóng khi hàm kết thúc. Nếu bạn không gọi hàm `free()`, vùng nhớ đó sẽ bị rò rỉ (Memory Leak), dẫn đến lãng phí RAM và có thể khiến chương trình bị treo hoặc tràn bộ nhớ khi chạy lâu dài.",
    "sources": [
      {
        "theoryId": "thy_dynamic_mem",
        "title": "Chương 3: Cấp phát bộ nhớ động với malloc và free",
        "snippet": "Bộ nhớ được cấp phát bởi malloc tồn tại cho đến khi được giải phóng rõ ràng bằng free()..."
      }
    ],
    "hasUnlockedContent": true
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Môn học hoặc nội dung câu hỏi không được để trống.`
- `403 Forbidden`: `Bạn chưa đăng ký môn học này hoặc chưa mở khóa các bài học liên quan.`
- `503 Service Unavailable`: `Dịch vụ AI hiện tại đang bận hoặc quá tải. Vui lòng thử lại sau giây lát.`
