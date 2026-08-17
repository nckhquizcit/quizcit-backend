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

### Cơ chế Xóa mềm (Soft Delete) & Bảo toàn dữ liệu
- **Soft Delete cho Content:** Các thực thể `Subject`, `Chapter`, `Theory` sử dụng cơ chế xóa mềm thông qua cột `deleted_at`. Khi bị xóa, dữ liệu vẫn được lưu trữ trong CSDL và tự động bị loại bỏ khỏi toàn bộ truy vấn của hệ thống.
- **Bảo toàn câu hỏi:** Khi xóa môn học hoặc chương, hệ thống **không xóa** các câu hỏi thuộc môn/chương đó mà tự động gỡ liên kết (`subject_id = null, chapter_id = null`) để lưu trong ngân hàng câu hỏi. Quản trị viên có thể sử dụng các API placement (`PATCH /api/questions/{questionId}/placement` hoặc `PATCH /api/questions/placement`) để di chuyển hoặc tái sử dụng câu hỏi cho các môn/chương khác.

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

**Cơ chế thực thi (Soft Delete):**
- **Xóa mềm môn học:** Đánh dấu `deleted_at = timestamp` và `is_visible = false`. Môn học bị ẩn hoàn toàn khỏi tất cả truy vấn người dùng.
- **Xóa toàn bộ chương (Soft Delete):** Đánh dấu xóa mềm tất cả chương thuộc môn và lý thuyết của các chương (đồng thời gỡ chỉ mục khỏi chatbot vector search), cấu hình kỹ năng chương, trạng thái mở khóa của học viên và đề thi (quizzes) thuộc môn.
- **Bảo toàn câu hỏi trong CSDL:** Tất cả câu hỏi thuộc môn học (và thuộc các chương của môn) **không bị xóa**, mà được tách ra thành câu hỏi độc lập (set `subject_id = null, chapter_id = null`) trong ngân hàng câu hỏi để quản trị viên có thể di chuyển / gán môn và chương mới.

**Response — `204 No Content`**.

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

**Cơ chế thực thi (Soft Delete):**
- **Bảo toàn câu hỏi:** Tất cả câu hỏi thuộc chương **không bị xóa**, mà được tách ra thành câu hỏi độc lập (set `subject_id = null, chapter_id = null`) trong ngân hàng câu hỏi.
- **Xóa mềm chương và lý thuyết:** Đánh dấu `deleted_at = timestamp` trên chương và các bài lý thuyết (đồng thời gỡ vector chunks khỏi chatbot), cấu hình kỹ năng chương, trạng thái mở khóa của học viên và đề thi (quizzes) thuộc chương.

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

Thực hiện **xóa mềm (soft delete)** bằng cách đánh dấu `deleted_at = timestamp`. Nội dung tương ứng cũng được gỡ khỏi chỉ mục chatbot vector search.

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

## 4. ❓ Question APIs

### 4.1 Tạo một câu hỏi

```http
POST /api/questions
Content-Type: application/json
```

**Quyền:** `ADMINISTRATOR`

**Request Body:**

```json
{
  "subjectId": "subject-id",
  "chapterId": "chapter-id",
  "content": "Nội dung plain text hoặc chuỗi BlockNote JSON",
  "explanation": "Lời giải/general feedback",
  "contentFormat": "plain | blocknote",
  "difficultyB": 0.0,
  "options": [
    { "content": "Đáp án A", "corrected": true },
    { "content": "Đáp án B", "corrected": false }
  ],
  "skills": [
    { "skillId": "skill-id", "weight": 1.0, "primary": true }
  ]
}
```

Quy tắc nghiệp vụ:

- `subjectId` và `chapterId` bắt buộc tồn tại; chapter phải thuộc đúng subject.
- `content` bắt buộc, tối đa `200000` ký tự.
- `explanation` tùy chọn, tối đa `200000` ký tự.
- Câu hỏi phải có ít nhất hai đáp án có nội dung và **đúng một** đáp án có
  `corrected = true`.
- Mỗi nội dung đáp án tối đa `200000` ký tự.
- `contentFormat` chỉ nhận diện giá trị `blocknote` (không phân biệt hoa/thường);
  mọi giá trị khác được chuẩn hóa thành `plain`. Format của câu hỏi được dùng
  chung cho câu hỏi, đáp án và lời giải.
- Nếu có `skills`, các skill phải thuộc chapter, không trùng nhau, tổng `weight`
  phải bằng `1` và có đúng một skill `primary`.

**Response — `201 Created`:** `ApiResponse<QuestionResponse>`.

### 4.2 Tạo nhiều câu hỏi (bulk import)

```http
POST /api/questions/bulk
Content-Type: application/json
```

**Quyền:** `ADMINISTRATOR`

API nhận tối đa **500 câu hỏi/request**. Mỗi phần tử được xử lý bằng transaction
riêng theo cơ chế **partial success**: một câu lỗi bị rollback riêng, các câu hợp
lệ khác vẫn được lưu.

**Request Body:**

```json
{
  "items": [
    {
      "clientId": "import-question-1",
      "question": {
        "subjectId": "subject-id",
        "chapterId": "chapter-id",
        "content": "[{\"id\":\"block-1\",\"type\":\"paragraph\",\"props\":{},\"content\":[{\"type\":\"text\",\"text\":\"Câu hỏi\",\"styles\":{}}],\"children\":[]}]",
        "explanation": "Lời giải hoặc general feedback",
        "contentFormat": "blocknote",
        "difficultyB": 0.0,
        "options": [
          { "content": "Đáp án đúng", "corrected": true },
          { "content": "Đáp án sai", "corrected": false }
        ],
        "skills": []
      }
    }
  ]
}
```

- `items` bắt buộc có ít nhất một phần tử và tối đa 500 phần tử.
- `clientId` bắt buộc, dùng để frontend ghép kết quả với đúng dòng preview.
- `question` tuân theo toàn bộ validation tại mục 4.1.
- Nếu cần import hơn 500 câu, client chia thành nhiều request, mỗi request tối đa
  500 câu.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "total": 3,
    "successCount": 2,
    "failureCount": 1,
    "items": [
      {
        "clientId": "import-question-1",
        "success": true,
        "questionId": "created-question-id",
        "message": null
      },
      {
        "clientId": "import-question-2",
        "success": false,
        "questionId": null,
        "message": "Câu hỏi phải có đúng một đáp án đúng"
      }
    ]
  }
}
```

Lỗi nghiệp vụ của từng câu được trả trong `data.items` và không đổi HTTP status
của toàn bộ request. Các lỗi ở cấp request, chẳng hạn `items` rỗng hoặc có hơn
500 phần tử, trả `400 Bad Request` theo response lỗi chuẩn.

### 4.3 Định dạng file import ở frontend

Backend không nhận trực tiếp file Moodle/GIFT/Aiken. Frontend đọc file, chuyển
dữ liệu sang request chuẩn ở mục 4.2, cho administrator preview rồi mới gọi API.

| Định dạng | Phạm vi hỗ trợ |
|---|---|
| Moodle XML (`.xml`) | `multichoice` một đáp án đúng và `truefalse`; HTML được chuyển sang BlockNote |
| GIFT (`.gift` hoặc `.txt`) | Single-choice, True/False, title, category, general feedback và nội dung `[html]` |
| Aiken (`.aiken` hoặc `.txt`) | 2–4 lựa chọn `A.`–`D.` và một dòng `ANSWER: A-D` |

Quy tắc chuyển đổi:

- Subject/chapter luôn lấy từ lựa chọn trên màn import; Moodle/GIFT category chỉ
  được hiển thị để tham khảo, không tự động map hoặc tạo chapter.
- Moodle `generalfeedback` và GIFT general feedback được lưu vào `explanation`.
- Câu nhiều đáp án đúng bị đánh lỗi riêng và không được gửi để lưu.
- Code HTML trong `<pre><code>` được chuyển thành BlockNote `codeBlock`; nếu
  không khai báo language thì mặc định là `c`.
- Chỉ giữ ảnh có URL `http` hoặc `https`. Ảnh Moodle dạng `@@PLUGINFILE@@` hoặc
  base64 chưa được hỗ trợ và tạo cảnh báo ở preview.
- Feedback riêng của từng đáp án chưa có trường lưu tương ứng nên bị bỏ qua và
  tạo cảnh báo.
- Các loại câu hỏi không biểu diễn được bằng model lựa chọn hiện tại, như essay,
  matching, short answer hoặc numerical, bị đánh lỗi riêng.

Frontend cung cấp file mẫu tải xuống cho cả Moodle XML, GIFT và Aiken.

### 4.4 Lấy câu hỏi

| Endpoint | Quyền | Nội dung |
|---|---|---|
| `GET /api/questions` | `ADMINISTRATOR` | Toàn bộ ngân hàng câu hỏi (kể cả câu hỏi chưa gán môn/chương `subjectId = null, chapterId = null`) |
| `GET /api/chapters/{chapterId}/questions` | Đã đăng nhập và có quyền truy cập chapter | Câu hỏi thuộc chapter |
| `GET /api/questions/{questionId}` | Đã đăng nhập | Chi tiết câu hỏi; đáp án đúng chỉ lộ cho administrator. Đối với câu hỏi mồ côi (`subjectId = null`), chỉ administrator xem được, user thông thường sẽ nhận `404 Not Found`. |

Response dùng `QuestionResponse`, gồm các trường chính `questionId`, `subjectId`,
`chapterId`, `content`, `explanation`, `contentFormat`, `difficultyB`, `options`,
`correctOptionId`, `skills` và `createdAt`. Các API danh sách có thể trả
`options` rỗng để tránh tải dữ liệu không cần thiết.

### 4.5 Cập nhật câu hỏi

```http
PUT /api/questions/{questionId}
```

**Quyền:** `ADMINISTRATOR`

Request có cấu trúc tương tự mục 4.1. `content` luôn bắt buộc. Nếu không gửi
`options` hoặc `skills` (`null`), backend giữ nguyên danh sách tương ứng; nếu gửi
danh sách, backend thay thế toàn bộ danh sách cũ.

**Gán lại môn và chương:**
- Có thể dùng endpoint này để gán môn học và chương mới cho câu hỏi mồ côi bằng cách truyền `subjectId` và `chapterId` hợp lệ.
- Nếu câu hỏi đang ở trạng thái mồ côi (`subjectId = null, chapterId = null`) mà request cập nhật không truyền đầy đủ `subjectId` và `chapterId`, hệ thống sẽ trả lỗi `400 Bad Request` (`"Vui lòng chọn môn học và chương cho câu hỏi"`).

**Response — `200 OK`:** message `Cập nhật câu hỏi thành công`, `data = null`.

### 4.6 Xóa câu hỏi

```http
DELETE /api/questions/{questionId}
```

**Quyền:** `ADMINISTRATOR`

Backend xóa các response, liên kết quiz, skill mapping và option liên quan trước
khi xóa câu hỏi.

**Response — `204 No Content`**.

### 4.7 Gắn nhanh môn học và chương cho câu hỏi

```http
PATCH /api/questions/{questionId}/placement
Content-Type: application/json
```

**Quyền:** `ADMINISTRATOR`

Dùng để gắn nhanh hoặc di chuyển câu hỏi (đặc biệt là câu hỏi mồ côi) sang môn học và chương mới mà không cần gửi lại nội dung câu hỏi hay options.

**Request Body:**

```json
{
  "subjectId": "string (required)",
  "chapterId": "string (required)"
}
```

- `subjectId` và `chapterId` bắt buộc không được để trống và phải tồn tại trong cơ sở dữ liệu.
- `chapterId` phải thuộc về `subjectId` tương ứng.
- Nếu câu hỏi có kỹ năng không tương thích với chương mới, các kỹ năng đó sẽ tự động được làm sạch.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Gắn môn học và chương cho câu hỏi thành công",
  "data": null
}
```

### 4.8 Gắn nhanh môn học và chương hàng loạt

```http
PATCH /api/questions/placement
Content-Type: application/json
```

**Quyền:** `ADMINISTRATOR`

Gán hàng loạt nhiều câu hỏi sang môn học và chương mới cùng lúc.

**Request Body:**

```json
{
  "questionIds": ["question-id-1", "question-id-2"],
  "subjectId": "string (required)",
  "chapterId": "string (required)"
}
```

- `questionIds` không được rỗng.
- Tất cả ID câu hỏi trong danh sách phải tồn tại.
- `chapterId` phải thuộc về `subjectId`.

**Response — `200 OK`:**

```json
{
  "status": 200,
  "message": "Gắn môn học và chương cho danh sách câu hỏi thành công",
  "data": null
}
```

---

## 5. 🧪 Danh sách đề theo môn học

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

## 6. 🎓 Course Enrollment APIs (`/api/users/me/course-enrollments`)

Tất cả API trong phần này yêu cầu access token của `USER` hoặc `ADMINISTRATOR`.
`userId` luôn được lấy từ token, client không truyền trong request.

### 6.1 Đăng ký môn học

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

### 6.2 Hủy đăng ký môn học

```http
DELETE /api/users/me/course-enrollments/{subjectId}
```

**Response — `204 No Content`**, không có body.

Nếu user chưa đăng ký môn này, API trả `404 Not Found` với message
`Bạn chưa đăng ký môn học này`.

### 6.3 Lấy danh sách môn đã đăng ký

```http
GET /api/users/me/course-enrollments
```

Danh sách được sắp theo `displayOrder`. User thông thường không thấy môn đã bị
ẩn; administrator vẫn thấy môn ẩn trong danh sách đăng ký của mình.

**Response — `200 OK`:** `ApiResponse<List<EnrolledSubjectResponse>>`, phần tử có
cấu trúc giống mục 6.1.

---

## 7. Mã lỗi thường gặp

| HTTP status | Trường hợp |
|---|---|
| `400 Bad Request` | Body sai định dạng; trường bắt buộc trống; bulk rỗng hoặc quá 500 câu; câu hỏi thiếu đáp án/không có đúng một đáp án đúng; reorder rỗng/sai thứ tự; skill trùng; `requiredTheta` không hữu hạn |
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
