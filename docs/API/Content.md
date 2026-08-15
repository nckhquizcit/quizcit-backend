Dưới đây là **API Spec** cho module Content của QuizCit Backend, được tổng hợp
trực tiếp từ `ContentController`, `CourseEnrollmentController`, các DTO và service
liên quan.

---

# 📘 QuizCit Backend — Content API Specification

## Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| **Base URL** | `http://<host>/api` |
| **Content-Type** | `application/json` |
| **Authentication** | `Authorization: Bearer <accessToken>` đối với API yêu cầu đăng nhập |
| **Response chuẩn** | Phần lớn API trả `ApiResponse<T>` với cấu trúc `{ status, message, data }` |

### Cấu trúc Response chuẩn (`ApiResponse`)

```json
// Thành công (có data)
{
  "status": 200,
  "message": "Success",
  "data": { ... }
}

// Thành công (không data)
{
  "status": 200,
  "message": "Thành công",
  "data": null
}

// Lỗi
{
  "status": 400,
  "message": "Mô tả lỗi",
  "data": null
}
```

Riêng `DELETE /api/users/me/course-enrollments/{subjectId}` trả response rỗng
`204 No Content`, không bọc trong `ApiResponse`.

### Quyền truy cập

| Nhóm API | Quyền |
|---|---|
| Xem danh sách/chi tiết môn học | Công khai; user chỉ thấy môn có `visible = true` |
| Xem chương, lý thuyết và danh sách đề | Yêu cầu đăng nhập và đã đăng ký môn học |
| Đăng ký/hủy đăng ký môn | `USER` hoặc `ADMINISTRATOR` đã đăng nhập |
| Tạo, sửa, xóa, sắp xếp và re-index nội dung | Chỉ `ADMINISTRATOR` |

- Administrator mặc định thấy cả môn ẩn và bỏ qua điều kiện đăng ký/mở khóa chương.
- Administrator có thể gửi header `X-QuizCIT-View-As: USER` để kiểm tra nội dung
  theo góc nhìn user thông thường.
- User truy cập môn ẩn hoặc chương chưa mở khóa sẽ nhận `404`, tránh làm lộ sự
  tồn tại của nội dung.
- `displayOrder` dùng số nguyên dương. Khi tạo/cập nhật, giá trị `null`, `0` hoặc
  âm được thay bằng thứ tự mặc định/thứ tự hiện tại; riêng API reorder bắt buộc
  giá trị lớn hơn `0`.

---

## 1. 📚 Subject APIs (`/api/subjects`)

### 1.1 Lấy danh sách môn học

```http
GET /api/subjects
```

API công khai. User chưa đăng nhập hoặc user thông thường chỉ thấy môn đang hiển
thị; administrator thấy tất cả môn.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "subjectId": "string",
      "courseCode": "string",
      "name": "string",
      "description": "string",
      "visible": true,
      "createdAt": "ZonedDateTime",
      "displayOrder": 1
    }
  ]
}
```

### 1.2 Lấy chi tiết môn học

```http
GET /api/subjects/{subjectId}
```

API công khai. Môn không tồn tại hoặc bị ẩn đối với user thường trả `404`.

**Response — `200 OK`:** `ApiResponse<SubjectResponse>`, có cấu trúc phần tử
giống mục 1.1.

### 1.3 Tạo môn học

```http
POST /api/subjects
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "courseCode": "string (required, max 20)",
  "name": "string (required, max 200)",
  "description": "string (optional)",
  "displayOrder": "integer (optional, > 0)"
}
```

Nếu bỏ trống `displayOrder`, backend tự đặt sau môn cuối cùng. Môn mới được tạo
với trạng thái hiển thị mặc định của entity.

**Response — `201 Created`:** `ApiResponse<SubjectResponse>`.

**Lỗi thường gặp:** `409 Conflict` nếu `courseCode` đã tồn tại.

### 1.4 Cập nhật môn học

```http
PUT /api/subjects/{subjectId}
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "courseCode": "string (required, max 20)",
  "name": "string (required, max 200)",
  "description": "string (optional)",
  "visible": true,
  "displayOrder": "integer (optional, > 0)"
}
```

Đây là request cập nhật toàn bộ: client cần gửi lại `courseCode`, `name` và
`visible`. Nếu `displayOrder` không hợp lệ hoặc không được gửi, thứ tự cũ được giữ.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Cập nhật học phần thành công",
  "data": null
}
```

### 1.5 Sắp xếp môn học

```http
PUT /api/subjects/display-order
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "items": [
    { "id": "subject-id-1", "displayOrder": 1 },
    { "id": "subject-id-2", "displayOrder": 2 }
  ]
}
```

`items` không được rỗng; mỗi `id` không được trống và `displayOrder` phải lớn hơn
`0`. Request có thể chỉ chứa các môn cần đổi thứ tự.

**Response — `200 OK`:** message `Đã cập nhật thứ tự môn học`, `data = null`.

### 1.6 Xóa môn học

```http
DELETE /api/subjects/{subjectId}
```

**Quyền:** `ADMINISTRATOR`

**Response — `204 No Content`**.

Controller tạo `ApiResponse` với message `Đã xóa thành công học phần`, nhưng theo
chuẩn HTTP body của response `204` có thể bị loại bỏ ở tầng server/client.

---

## 2. 📖 Chapter APIs

### 2.1 Lấy tất cả chương có thể truy cập

```http
GET /api/chapters
```

**Quyền:** Đã đăng nhập.

Trả các chương thuộc những môn user đã đăng ký và đã đủ điều kiện mở khóa.
Administrator nhận toàn bộ chương của toàn bộ môn.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "chapterId": "string",
      "subjectId": "string",
      "name": "string",
      "createdAt": "ZonedDateTime",
      "displayOrder": 1,
      "skills": [
        {
          "skillId": "string",
          "name": "string",
          "requiredTheta": 0.5
        }
      ]
    }
  ]
}
```

### 2.2 Lấy danh sách chương của một môn

```http
GET /api/subjects/{subjectId}/chapters
```

**Quyền:** Đã đăng nhập và đã đăng ký môn học.

Chỉ các chương đã mở khóa được trả về. Một chương được mở khóa khi user đã từng
được ghi nhận mở khóa, hoặc năng lực `theta` của user đạt `requiredTheta` cho
**tất cả** kỹ năng yêu cầu. Chương không có kỹ năng yêu cầu được mở khóa mặc định.

**Response — `200 OK`:** `ApiResponse<List<ChapterResponse>>`, cùng cấu trúc mục 2.1.

### 2.3 Lấy chi tiết chương

```http
GET /api/chapters/{chapterId}
```

**Quyền:** Đã đăng nhập, đã đăng ký môn và chương đã mở khóa.

**Response — `200 OK`:** `ApiResponse<ChapterResponse>`.

### 2.4 Thêm chương vào môn học

```http
POST /api/subjects/{subjectId}/chapters
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "name": "string (required, max 200)",
  "displayOrder": "integer (optional, > 0)",
  "skills": [
    {
      "skillId": "string (required)",
      "requiredTheta": "number (required, finite)"
    }
  ]
}
```

`skills` là tùy chọn. Các `skillId` phải tồn tại và không được trùng nhau.

**Response — `201 Created`:** `ApiResponse<ChapterResponse>`.

### 2.5 Cập nhật chương

```http
PUT /api/chapters/{chapterId}
```

**Quyền:** `ADMINISTRATOR`

**Request Body:** giống mục 2.4.

- `name` luôn bắt buộc.
- Nếu không gửi `skills` (`null`), backend giữ nguyên kỹ năng hiện tại.
- Nếu gửi `skills: []`, backend xóa toàn bộ yêu cầu kỹ năng.
- Không thể xóa một kỹ năng đang được câu hỏi trong chương sử dụng; API trả `409`.

**Response — `200 OK`:** message `Cập nhật chương thành công`, `data = null`.

### 2.6 Sắp xếp chương trong môn học

```http
PUT /api/subjects/{subjectId}/chapters/display-order
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "items": [
    { "id": "chapter-id-1", "displayOrder": 1 },
    { "id": "chapter-id-2", "displayOrder": 2 }
  ]
}
```

Mọi chapter trong request phải thuộc đúng `subjectId` trên URL.

**Response — `200 OK`:** message `Đã cập nhật thứ tự chương`, `data = null`.

### 2.7 Xóa chương

```http
DELETE /api/chapters/{chapterId}
```

**Quyền:** `ADMINISTRATOR`

**Response — `204 No Content`**.

---

## 3. 📝 Theory APIs

### 3.1 Lấy danh sách lý thuyết của chương

```http
GET /api/chapters/{chapterId}/theories
```

**Quyền:** Đã đăng nhập, đã đăng ký môn và chương đã mở khóa.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "theoryId": "string",
      "chapterId": "string",
      "content": "string",
      "contentFormat": "plain | blocknote",
      "createdAt": "ZonedDateTime"
    }
  ]
}
```

### 3.2 Lấy toàn bộ lý thuyết của môn học

```http
GET /api/subjects/{subjectId}/theories
```

**Quyền:** Đã đăng nhập và đã đăng ký môn học.

Chỉ lý thuyết thuộc các chương user đã mở khóa được trả về.

**Response — `200 OK`:** `ApiResponse<List<TheoryResponse>>`.

### 3.3 Lấy chi tiết lý thuyết

```http
GET /api/theories/{theoryId}
```

**Quyền:** Đã đăng nhập, đã đăng ký môn và chương chứa lý thuyết đã mở khóa.

**Response — `200 OK`:** `ApiResponse<TheoryResponse>`.

### 3.4 Thêm lý thuyết vào chương

```http
POST /api/chapters/{chapterId}/theories
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "content": "string (required)",
  "contentFormat": "plain | blocknote (optional, default plain)"
}
```

Chỉ giá trị `blocknote` (không phân biệt hoa/thường) được chuẩn hóa thành
`blocknote`; mọi giá trị khác, kể cả `null`, được lưu dưới dạng `plain`. Nội dung
mới đồng thời được đưa vào chỉ mục phục vụ chatbot.

**Response — `201 Created`:** `ApiResponse<TheoryResponse>`.

### 3.5 Cập nhật lý thuyết

```http
PUT /api/theories/{theoryId}
```

**Quyền:** `ADMINISTRATOR`

**Request Body:** giống mục 3.4. Sau cập nhật, chỉ mục chatbot được tạo lại.

**Response — `200 OK`:** message `Cập nhật nội dung lý thuyết thành công`,
`data = null`.

### 3.6 Xóa lý thuyết

```http
DELETE /api/theories/{theoryId}
```

**Quyền:** `ADMINISTRATOR`

Nội dung tương ứng cũng được xóa khỏi chỉ mục chatbot.

**Response — `204 No Content`**.

### 3.7 Re-index lý thuyết theo môn học

```http
POST /api/subjects/{subjectId}/theories/reindex
```

**Quyền:** `ADMINISTRATOR`

Tạo lại chỉ mục chatbot cho toàn bộ lý thuyết thuộc môn. `data` là tổng số chunk
đã được đánh chỉ mục, không nhất thiết bằng số theory.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Re-indexed 12 theory chunks",
  "data": 12
}
```

---

## 4. 🧪 Danh sách đề theo môn học

```http
GET /api/subjects/{subjectId}/quizzes
```

**Quyền:** Đã đăng nhập và đã đăng ký môn học.

Chỉ trả các quiz không thích ứng (non-adaptive), ở dạng rút gọn và không kèm danh
sách câu hỏi.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "quizId": "string",
      "name": "string",
      "subjectId": "string",
      "chapterId": "string",
      "totalQuestion": 20,
      "duration": 30,
      "type": "CHAPTER | FINAL",
      "displayOrder": 1,
      "createdAt": "ZonedDateTime"
    }
  ]
}
```

---

## 5. 🎓 Course Enrollment APIs (`/api/users/me/course-enrollments`)

Tất cả API trong phần này yêu cầu access token của `USER` hoặc `ADMINISTRATOR`.
`userId` luôn được lấy từ token, client không truyền trong request.

### 5.1 Đăng ký môn học

```http
POST /api/users/me/course-enrollments/{subjectId}
```

Chỉ đăng ký được môn đang hiển thị đối với user thông thường.

**Response — `201 Created`:**

```json
{
  "status": 201,
  "message": "Đăng ký môn học thành công",
  "data": {
    "subjectId": "string",
    "courseCode": "string",
    "name": "string",
    "description": "string",
    "createdAt": "ZonedDateTime",
    "displayOrder": 1,
    "enrolledAt": "ZonedDateTime"
  }
}
```

Đăng ký lại môn đã đăng ký trả `409 Conflict` với message
`Bạn đã đăng ký môn học này`.

### 5.2 Hủy đăng ký môn học

```http
DELETE /api/users/me/course-enrollments/{subjectId}
```

**Response — `204 No Content`**, không có body.

Nếu user chưa đăng ký môn này, API trả `404 Not Found` với message
`Bạn chưa đăng ký môn học này`.

### 5.3 Lấy danh sách môn đã đăng ký

```http
GET /api/users/me/course-enrollments
```

Danh sách được sắp theo `displayOrder`. User thông thường không thấy môn đã bị
ẩn; administrator vẫn thấy môn ẩn trong danh sách đăng ký của mình.

**Response — `200 OK`:** `ApiResponse<List<EnrolledSubjectResponse>>`, phần tử có
cấu trúc giống mục 5.1.

---

## 6. Mã lỗi thường gặp

| HTTP status | Trường hợp |
|---|---|
| `400 Bad Request` | Body sai định dạng; trường bắt buộc trống; reorder rỗng/sai thứ tự; skill trùng; `requiredTheta` không hữu hạn |
| `401 Unauthorized` | Chưa đăng nhập hoặc access token không hợp lệ |
| `403 Forbidden` | Chưa đăng ký môn; không có quyền `ADMINISTRATOR` khi gọi API quản trị |
| `404 Not Found` | Không tìm thấy môn/chương/lý thuyết/skill; môn bị ẩn; chương chưa mở khóa |
| `409 Conflict` | Trùng mã học phần; đã đăng ký môn; xóa skill đang được câu hỏi sử dụng |

Ví dụ user đã đăng nhập nhưng chưa đăng ký môn:

```json
{
  "status": 403,
  "message": "Bạn cần đăng ký môn học trước khi truy cập nội dung này",
  "data": null
}
```
