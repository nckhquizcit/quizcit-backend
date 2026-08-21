# 📚 QuizCit Backend — Tổng quan Tài liệu API & Kiến trúc Hệ thống

Hệ thống **QuizCit Backend** cung cấp bộ RESTful API toàn diện cho nền tảng học tập, ôn luyện trắc nghiệm, đánh giá năng lực theo mô hình Lý thuyết Ứng đáp Câu hỏi (IRT - Item Response Theory) và học tập thích ứng (Computerized Adaptive Testing - CAT).

---

## 1. 🌐 Thông tin chung & Quy chuẩn API

| Thuộc tính | Giá trị |
|---|---|
| **Base URL** | `http://<host>:<port>/api` (Mặc định: `http://localhost:8080/api`) |
| **Giao thức** | HTTP / HTTPS |
| **Định dạng dữ liệu** | `application/json; charset=UTF-8` (ngoại trừ API upload file dùng `multipart/form-data`) |
| **Xác thực** | Bearer Token qua Header: `Authorization: Bearer <accessToken>` |
| **Chuẩn phản hồi** | Luôn bao bọc trong đối tượng chuẩn `ApiResponse<T>` |

---

## 2. 📦 Cấu trúc Response chuẩn (`ApiResponse<T>`)

Mọi phản hồi từ QuizCit Backend đều tuân thủ định dạng chuẩn sau:

### 2.1 Thành công có dữ liệu (`200 OK`, `201 Created`)
```json
{
  "status": 200,
  "message": "Thao tác thành công",
  "data": {
    "id": "123",
    "name": "Dữ liệu mẫu"
  }
}
```

### 2.2 Thành công không có dữ liệu trả về (`200 OK`, `204 No Content`)
```json
{
  "status": 200,
  "message": "Cập nhật thành công",
  "data": null
}
```

### 2.3 Lỗi nghiệp vụ hoặc hệ thống (`400`, `401`, `403`, `404`, `409`, `429`, `500`)
```json
{
  "status": 400,
  "message": "Thông điệp mô tả lỗi chi tiết cho người dùng / lập trình viên",
  "data": null
}
```

### 2.4 Cấu trúc dữ liệu phân trang (`PageResponse<T>`)
Đối với các endpoint danh sách có phân trang (`Pageable`):
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "items": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 145,
    "totalPages": 8,
    "first": true,
    "last": false
  }
}
```

---

## 3. 🔐 Phân quyền & Vai trò người dùng (RBAC)

| Vai trò / Trạng thái | Mã (`UserRole` / `UserStatus`) | Mô tả quyền hạn |
|---|---|---|
| **Public / Guest** | Không cần Token | Đăng ký, đăng nhập, quên mật khẩu, xem danh sách môn học công khai. |
| **Học viên (`USER`)** | `USER` (`VERIFIED`) | Xem môn học, đăng ký môn học, làm bài kiểm tra tiêu chuẩn & thích ứng, xem lịch sử/tiến độ cá nhân, đổi mật khẩu/email, hỏi đáp AI Chatbot. |
| **Quản trị viên (`ADMINISTRATOR`)** | `ADMINISTRATOR` (`VERIFIED`) | Toàn quyền quản trị hệ thống: Quản lý người dùng, phân quyền, cấu hình môn học, chương, lý thuyết, kỹ năng, ngân hàng câu hỏi, đề thi, reindex vector database, xem thống kê hệ thống. |

---

## 4. 🗑️ Cơ chế Xóa mềm (Soft Delete) & Xóa cứng (Hard Delete)

Hệ thống hỗ trợ 2 cơ chế xóa trên các thực thể:

1. **Xóa mềm (Mặc định - `DELETE /api/...`):**
   - Đánh dấu `deleted_at = CURRENT_TIMESTAMP`.
   - Tự động lọc ẩn thông qua Hibernate `@SQLRestriction("deleted_at IS NULL")`.
   - Bảo toàn tính toàn vẹn dữ liệu lịch sử thi, kết quả IRT và thống kê của học viên trong quá khứ.
2. **Xóa cứng (Vĩnh viễn - `DELETE /api/...?hard=true`):**
   - Áp dụng khi quản trị viên muốn xóa sạch hoàn toàn thực thể và cascade dọn dẹp các bảng phụ thuộc.
   - **Quy tắc bảo vệ Quiz:** Đề thi đã có lượt làm bài (`attempts`) **bị cấm xóa cứng** (Backend trả lỗi `400 Bad Request` yêu cầu dùng xóa mềm để ẩn).

---

## 5. 📑 Danh mục tài liệu chi tiết theo Module

Tài liệu API được chia thành các chuyên đề chi tiết sau:

1. **[01. Xác thực, Người dùng & Phân quyền (Auth & User Module)](API/01_Auth_and_User.md)**
   - Đăng ký, Đăng nhập, OTP Email, Đổi mật khẩu, Refresh Token, Đổi email 2 bước.
   - Quản lý người dùng, Khóa/Mở tài khoản, Đổi vai trò, Xóa mềm & Xóa cứng User.
   - Quản lý Vai trò (Roles), Quyền hạn (Permissions).
   - Thống kê dữ liệu tổng quan cho Admin Dashboard (`/api/admin/data-stats`).
2. **[02. Nội dung Học tập & Đăng ký Môn học (Content & Course Module)](API/02_Content_and_Course.md)**
   - Quản lý Môn học (Subject), Chương (Chapter), Nội dung Lý thuyết (Theory).
   - Sắp xếp thứ tự hiển thị (Reorder Display Order), Đánh chỉ mục Vector Search (`reindex`).
   - Đăng ký môn học (Course Enrollments).
   - Tải lên hình ảnh và tệp phương tiện (Media Uploads).
3. **[03. Quản lý Kỹ năng & Tiến độ Học tập (Skills & Progress Module)](API/03_Skills_and_Progress.md)**
   - Quản lý Kỹ năng (Skill CRUD), Nhập kỹ năng hàng loạt từ file CSV (`/api/skills/bulk`).
   - Theo dõi năng lực học viên theo kỹ năng (IRT Ability $\theta$), Lịch sử biến thiên năng lực, Điểm năng lực trung bình.
4. **[04. Ngân hàng Câu hỏi & Đề thi (Quiz & Questions Module)](API/04_Quiz_and_Questions.md)**
   - Quản lý Ngân hàng Câu hỏi (Question CRUD), Gán môn/chương/kỹ năng, Nhập câu hỏi hàng loạt (Bulk Import).
   - Quản lý Đề thi tiêu chuẩn (Quiz CRUD), Cấu hình danh sách câu hỏi trong đề, Sắp xếp đề thi, Quy tắc xóa mềm/xóa cứng an toàn.
5. **[05. Làm bài Kiểm tra & Học thích ứng (Attempt & Adaptive Learning Module)](API/05_Attempt_and_Adaptive_Learning.md)**
   - Quy trình làm bài kiểm tra tiêu chuẩn (Start Attempt, Submit, View Result).
   - Cơ chế Học thích ứng (CAT - Computerized Adaptive Testing): Kiểm tra điều kiện mở khóa (`adaptive-eligibility`), Làm bài theo batch 5 câu tự động điều chỉnh độ khó theo $\theta$, Cập nhật điểm năng lực EAP/MLE.
6. **[06. Trợ lý AI & Chatbot RAG (AI & Chatbot Module)](API/06_AI_and_Chatbot.md)**
   - Ước lượng năng lực IRT qua AI Service (`/api/ai/estimate`).
   - Hỏi đáp trợ giảng AI Chatbot tích hợp RAG truy xuất ngữ cảnh lý thuyết qua Vector Database (`/api/chatbot/ask`).

---

## 6. 🚨 Bảng mã lỗi HTTP phổ biến

| HTTP Status | Ý nghĩa | Khi nào xảy ra? |
|---|---|---|
| `200 OK` | Thành công | Yêu cầu xử lý thành công và có trả về dữ liệu hoặc thông báo. |
| `201 Created` | Đã tạo thành công | Tạo mới tài nguyên thành công (Đăng ký, tạo môn học, tạo câu hỏi, bắt đầu lượt thi...). |
| `204 No Content` | Thành công không nội dung | Xóa tài nguyên thành công hoặc thao tác không cần trả về body. |
| `400 Bad Request` | Dữ liệu không hợp lệ / Lỗi nghiệp vụ | Thiếu tham số bắt buộc, sai định dạng JSON, mật khẩu yếu, đề thi đã có người làm không cho xóa cứng, quiz chưa đủ điều kiện... |
| `401 Unauthorized` | Chưa xác thực | Không truyền `Authorization: Bearer <token>`, Token hết hạn hoặc không hợp lệ. |
| `403 Forbidden` | Không có quyền truy cập | Người dùng thường cố truy cập API Admin, hoặc truy cập tài nguyên/lượt thi của người khác. |
| `404 Not Found` | Không tìm thấy | ID môn học, câu hỏi, đề thi, lượt thi hoặc người dùng không tồn tại hoặc đã bị xóa. |
| `409 Conflict` | Xung đột dữ liệu | Username hoặc Email đã được đăng ký trước đó. |
| `429 Too Many Requests` | Vượt quá giới hạn gọi API | Gửi OTP quá nhiều lần trong thời gian ngắn (Rate Limiting). |
| `500 Internal Server Error` | Lỗi máy chủ nội bộ | Lỗi hệ thống chưa được xử lý ngoại lệ (Database down, lỗi logic ngoài dự tính). |
