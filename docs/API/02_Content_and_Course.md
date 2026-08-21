# 📖 Module 2: Nội dung Học tập & Đăng ký Môn học (Content & Course API Specification)

Tài liệu chi tiết các endpoint quản lý Môn học (`/api/subjects`), Chương học (`/api/chapters`), Lý thuyết (`/api/theories`), Đăng ký môn học (`/api/users/me/course-enrollments`) và Quản lý Tải lên Phương tiện (`/api/uploads`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `GET` | `/api/subjects` | Public / User | Lấy danh sách tất cả môn học (đã lọc các môn đang hiển thị). |
| `GET` | `/api/subjects/{subjectId}` | Public / User | Lấy chi tiết thông tin một môn học. |
| `POST` | `/api/subjects` | `ADMINISTRATOR` | Tạo mới một môn học. |
| `PUT` | `/api/subjects/{subjectId}` | `ADMINISTRATOR` | Cập nhật thông tin môn học (tên, mã môn, mô tả, ảnh, hiển thị). |
| `PUT` | `/api/subjects/display-order` | `ADMINISTRATOR` | Cập nhật thứ tự sắp xếp (display order) các môn học. |
| `DELETE` | `/api/subjects/{subjectId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) môn học. |
| `GET` | `/api/subjects/{subjectId}/quizzes` | `isAuthenticated()` | Lấy danh sách đề thi thuộc môn học. |
| `GET` | `/api/subjects/{subjectId}/chapters` | `isAuthenticated()` | Lấy danh sách các chương thuộc môn học (yêu cầu ghi danh). |
| `GET` | `/api/chapters` | `isAuthenticated()` | Lấy danh sách tất cả các chương thuộc các môn người dùng được phép truy cập. |
| `GET` | `/api/chapters/{chapterId}` | `isAuthenticated()` | Lấy chi tiết thông tin một chương học. |
| `POST` | `/api/subjects/{subjectId}/chapters` | `ADMINISTRATOR` | Thêm chương học mới vào môn học. |
| `PUT` | `/api/chapters/{chapterId}` | `ADMINISTRATOR` | Cập nhật thông tin chương học và danh sách kỹ năng liên kết. |
| `PUT` | `/api/subjects/{subjectId}/chapters/display-order` | `ADMINISTRATOR` | Cập nhật thứ tự sắp xếp các chương trong một môn học. |
| `DELETE` | `/api/chapters/{chapterId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) chương học. |
| `GET` | `/api/chapters/{chapterId}/theories` | `isAuthenticated()` | Lấy danh sách các bài lý thuyết thuộc chương học. |
| `GET` | `/api/subjects/{subjectId}/theories` | `isAuthenticated()` | Lấy toàn bộ các bài lý thuyết trong một môn học. |
| `GET` | `/api/theories/{theoryId}` | `isAuthenticated()` | Lấy chi tiết một bài lý thuyết (kèm nội dung BlockNote/Markdown). |
| `POST` | `/api/chapters/{chapterId}/theories` | `ADMINISTRATOR` | Tạo bài lý thuyết mới và tự động đánh chỉ mục vector. |
| `PUT` | `/api/theories/{theoryId}` | `ADMINISTRATOR` | Cập nhật nội dung bài lý thuyết và reindex vector. |
| `DELETE` | `/api/theories/{theoryId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) bài lý thuyết. |
| `POST` | `/api/subjects/{subjectId}/theories/reindex` | `ADMINISTRATOR` | Đánh lại chỉ mục vector toàn bộ lý thuyết của môn học cho Chatbot AI. |
| `POST` | `/api/users/me/course-enrollments/{subjectId}` | `isAuthenticated()` | Đăng ký (Ghi danh) vào một môn học. |
| `DELETE` | `/api/users/me/course-enrollments/{subjectId}` | `isAuthenticated()` | Hủy đăng ký (Rút lui) khỏi môn học. |
| `GET` | `/api/users/me/course-enrollments` | `isAuthenticated()` | Lấy danh sách các môn học mà người dùng hiện tại đã đăng ký. |
| `POST` | `/api/uploads/image` | `ADMINISTRATOR` | Tải lên hình ảnh dạng `multipart/form-data`. |
| `POST` | `/api/uploads/from-url` | `ADMINISTRATOR` | Tải và lưu trữ hình ảnh từ đường dẫn URL bên ngoài. |
| `GET` | `/api/uploads/{folder}/{fileName}` | Public | Truy xuất hình ảnh tĩnh (có cache trình duyệt 30 ngày). |

---

## 2. 📚 Chi tiết Quản lý Môn học (`/api/subjects`)

### 2.1 Lấy danh sách môn học (`GET /api/subjects`)
- **Usecase:** Trang chủ và trang danh mục môn học gọi API này để hiển thị danh sách các môn học đang hoạt động.
- **Quyền:** Public.

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "subjectId": "sub_ct101",
      "courseCode": "CT101",
      "name": "Lập trình C cơ bản",
      "description": "Nhập môn lập trình, con trỏ, cấu trúc dữ liệu cơ bản",
      "imageUrl": "http://localhost:8080/api/uploads/subjects/c-lang.png",
      "displayOrder": 1,
      "visible": true,
      "chapterCount": 5
    }
  ]
}
```

---

### 2.2 Tạo & Cập nhật Môn học (`POST / PUT /api/subjects`)
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "courseCode": "CT101",
  "name": "Lập trình C cơ bản",
  "description": "Nhập môn lập trình, con trỏ, cấu trúc dữ liệu cơ bản",
  "imageUrl": "http://localhost:8080/api/uploads/subjects/c-lang.png",
  "visible": true
}
```
*Ghi chú:*
- `courseCode`: Mã môn học (Ví dụ: `CT101`, `CS202`), bắt buộc, tối đa 50 ký tự, không trùng lặp giữa các môn đang hoạt động.
- `name`: Tên môn học, bắt buộc, tối đa 255 ký tự.
- `visible`: Trạng thái bật/tắt hiển thị với học viên.

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Mã học phần đã tồn tại trong hệ thống.`, `Tên học phần không được để trống.`
- `404 Not Found`: Khi cập nhật môn học không tồn tại.

---

### 2.3 Sắp xếp thứ tự môn học (`PUT /api/subjects/display-order`)
- **Usecase:** Kéo thả sắp xếp thứ tự môn học trên giao diện quản trị Admin.
- **Request Body:**
```json
{
  "items": [
    { "id": "sub_ct101", "displayOrder": 1 },
    { "id": "sub_cs202", "displayOrder": 2 }
  ]
}
```

---

### 2.4 Xóa Môn học (`DELETE /api/subjects/{subjectId}`)
- **Query Params:**
  - `hard`: `false` (mặc định - Xóa mềm môn học, chuyển toàn bộ câu hỏi liên quan về Ngân hàng câu hỏi mặc định).
  - `hard`: `true` (Xóa cứng: dọn dẹp triệt để vector embedding, chương, lý thuyết, đề thi và bảng ghi danh).
- **Quy tắc an toàn:** Không thể xóa môn học hệ thống "Ngân hàng câu hỏi mặc định" (`400 Bad Request`).

---

## 3. 📑 Chi tiết Quản lý Chương học (`/api/chapters`)

### 3.1 Lấy danh sách chương của môn học (`GET /api/subjects/{subjectId}/chapters`)
- **Usecase:** Học viên xem lộ trình các chương bài học sau khi đã ghi danh môn học.
- **Quyền:** Học viên đã đăng ký môn học hoặc `ADMINISTRATOR`.

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "chapterId": "chp_pointer",
      "subjectId": "sub_ct101",
      "name": "Chương 3: Con trỏ và Cấp phát bộ nhớ động",
      "description": "Hiểu về địa chỉ ô nhớ, toán tử & và *, malloc, free",
      "displayOrder": 3,
      "skillIds": [
        "skl_pointer_basic",
        "skl_dynamic_memory"
      ],
      "theoryCount": 4,
      "questionCount": 25
    }
  ]
}
```

#### Các lỗi có thể gặp:
- `403 Forbidden`: `Bạn cần đăng ký môn học này trước khi xem danh sách bài học.`

---

### 3.2 Thêm & Cập nhật Chương học (`POST / PUT`)
- **Tạo:** `POST /api/subjects/{subjectId}/chapters`
- **Sửa:** `PUT /api/chapters/{chapterId}`
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "name": "Chương 3: Con trỏ và Cấp phát bộ nhớ động",
  "description": "Hiểu về địa chỉ ô nhớ, toán tử & và *, malloc, free",
  "skillIds": [
    "skl_pointer_basic",
    "skl_dynamic_memory"
  ]
}
```

---

### 3.3 Sắp xếp thứ tự chương (`PUT /api/subjects/{subjectId}/chapters/display-order`)
- **Request Body:**
```json
{
  "items": [
    { "id": "chp_intro", "displayOrder": 1 },
    { "id": "chp_array", "displayOrder": 2 },
    { "id": "chp_pointer", "displayOrder": 3 }
  ]
}
```

---

### 3.4 Xóa Chương học (`DELETE /api/chapters/{chapterId}`)
- **Query Params:** `?hard=true` (xóa vĩnh viễn) hoặc `?hard=false` (xóa mềm).
- **Cơ chế:** Tự động điều chuyển câu hỏi của chương về Ngân hàng câu hỏi mặc định để tránh mất dữ liệu câu hỏi.

---

## 4. 📝 Chi tiết Quản lý Lý thuyết (`/api/theories`)

### 4.1 Lấy chi tiết bài lý thuyết (`GET /api/theories/{theoryId}`)
- **Response `200 OK`:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "theoryId": "thy_malloc_guide",
    "chapterId": "chp_pointer",
    "title": "Hướng dẫn sử dụng hàm malloc() và free() trong C",
    "content": "[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hàm malloc cấp phát bộ nhớ trong vùng Heap...\"}]}]",
    "contentFormat": "BLOCKNOTE",
    "createdAt": "2026-08-21T08:00:00Z",
    "updatedAt": "2026-08-21T09:30:00Z"
  }
}
```

---

### 4.2 Tạo & Cập nhật Lý thuyết (`POST / PUT`)
- **Tạo:** `POST /api/chapters/{chapterId}/theories`
- **Sửa:** `PUT /api/theories/{theoryId}`
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "title": "Hướng dẫn sử dụng hàm malloc() và free() trong C",
  "content": "[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Nội dung chi tiết của bài học...\"}]}]",
  "contentFormat": "BLOCKNOTE"
}
```
*Ghi chú:* `contentFormat` có thể là `BLOCKNOTE` (JSON cấu trúc) hoặc `MARKDOWN` / `PLAIN_TEXT`. Khi lưu, backend tự động phân đoạn văn bản và gọi AI Embedding Service để nạp vector vào bảng `theory_chunks`.

---

### 4.3 Đánh lại Vector Index cho Môn học (`POST /api/subjects/{subjectId}/theories/reindex`)
- **Usecase:** Dùng khi vừa cập nhật thuật toán sinh vector embedding hoặc chuyển đổi model AI RAG để đánh lại toàn bộ các đoạn lý thuyết của môn học.
- **Quyền:** `ADMINISTRATOR`
- **Response `200 OK`:**
```json
{
  "status": 200,
  "message": "Re-indexed 48 theory chunks",
  "data": 48
}
```

---

## 5. 🎓 Đăng ký Môn học (`/api/users/me/course-enrollments`)

### 5.1 Đăng ký môn học (`POST /api/users/me/course-enrollments/{subjectId}`)
- **Usecase:** Học viên nhấn "Đăng ký học" trên giao diện môn học để mở khóa toàn bộ lộ trình chương, lý thuyết và bài kiểm tra.
- **Quyền:** `isAuthenticated()`
- **Response `201 Created`:**
```json
{
  "status": 201,
  "message": "Đăng ký môn học thành công",
  "data": {
    "subjectId": "sub_ct101",
    "courseCode": "CT101",
    "name": "Lập trình C cơ bản",
    "enrolledAt": "2026-08-21T11:00:00Z"
  }
}
```

---

### 5.2 Hủy đăng ký môn học (`DELETE /api/users/me/course-enrollments/{subjectId}`)
- **Usecase:** Học viên muốn rời khỏi môn học.
- **Response `204 No Content`**.

---

### 5.3 Danh sách môn học đã đăng ký (`GET /api/users/me/course-enrollments`)
- **Response `200 OK`:** Trả về danh sách các môn học học viên đang tham gia.

---

## 6. 🖼️ Quản lý Tải lên Phương tiện (`/api/uploads`)

### 6.1 Tải ảnh lên (`POST /api/uploads/image`)
- **Content-Type:** `multipart/form-data`
- **Quyền:** `ADMINISTRATOR`
- **Form Data:**
  - `file`: Tệp hình ảnh (`.png`, `.jpg`, `.jpeg`, `.webp`, tối đa 5MB).
  - `folder`: Thư mục lưu trữ (Ví dụ: `subjects`, `theories`, `questions`, `avatars`). Mặc định: `subjects`.

#### Response `201 Created`:
```json
{
  "status": 201,
  "message": "Tải ảnh lên thành công",
  "data": {
    "url": "http://localhost:8080/api/uploads/theories/pointer-diagram-123456.png",
    "fileName": "pointer-diagram-123456.png",
    "size": 245890,
    "contentType": "image/png"
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Tệp tải lên không phải là định dạng hình ảnh hợp lệ.`, `Dung lượng tệp vượt quá giới hạn 5MB.`

---

### 6.2 Truy xuất tệp tĩnh (`GET /api/uploads/{folder}/{fileName}`)
- **Quyền:** Public.
- **Header phản hồi:** `Cache-Control: public, max-age=2592000` (Cache 30 ngày) và `Content-Type` chuẩn của tệp ảnh.
