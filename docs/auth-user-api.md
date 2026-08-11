# QuizCIT Auth & User API

Tài liệu này mô tả API hiện hành của module Auth, User và đăng ký môn học của
người dùng. Nội dung được đối chiếu trực tiếp với controller, DTO, security và
service trong backend.

## 1. Quy ước chung

### Base URL

```text
http://localhost:8080/api
```

### Content type

```http
Content-Type: application/json
```

### Authentication

Các endpoint được đánh dấu `Authenticated` hoặc `Administrator` yêu cầu:

```http
Authorization: Bearer <accessToken>
```

- Access token mặc định hết hạn sau 15 phút.
- Refresh token mặc định hết hạn sau 30 ngày.
- Refresh token được rotate sau mỗi lần refresh; token cũ không dùng lại được.
- Đổi mật khẩu, đổi email, đổi trạng thái hoặc đổi role sẽ vô hiệu hóa các token cũ.

### Response envelope

Các response có body sử dụng cấu trúc:

```json
{
  "status": 200,
  "message": "Success",
  "data": {}
}
```

Lưu ý: `status` trong body tương ứng HTTP status, ngoại trừ các endpoint
`204 No Content` không trả response body.

### Validation error

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "fieldName": "Thông báo lỗi"
  }
}
```

### Application error

```json
{
  "status": 401,
  "message": "Thông báo lỗi",
  "data": null
}
```

Các status thường gặp:

| Status | Ý nghĩa |
|---:|---|
| `400` | Request/OTP/token xác nhận không hợp lệ |
| `401` | Sai thông tin đăng nhập, sai OTP hoặc access/refresh token không hợp lệ |
| `403` | Không đủ quyền, tài khoản bị khóa/chưa xác thực |
| `404` | Không tìm thấy user hoặc tài nguyên |
| `409` | Username/email đã tồn tại hoặc dữ liệu xung đột |
| `429` | Vượt rate limit, gửi OTP quá sớm hoặc nhập OTP sai quá số lần cho phép |
| `500` | Lỗi hệ thống hoặc cấu hình role bị thiếu |

### Enum

```text
UserRole   = USER | ADMINISTRATOR
UserStatus = UNVERIFIED | VERIFIED | BANNED
```

### UserResponse

```json
{
  "userId": "7a37c23a-14bb-4ed8-b4c9-4c028973a087",
  "username": "student01",
  "email": "student01@example.com",
  "displayName": "Nguyễn Văn A",
  "role": "USER",
  "status": "VERIFIED",
  "createdAt": "2026-08-11T20:00:00+07:00"
}
```

## 2. Auth API

Tất cả endpoint `/api/auth/**` đều public ở tầng HTTP security. Những endpoint
cấp token vẫn tự kiểm tra trạng thái tài khoản và thông tin xác thực.

### 2.1 Đăng ký

```http
POST /api/auth/register
```

Quyền: `Public`

Request:

```json
{
  "username": "student01",
  "password": "secret123",
  "email": "student01@example.com",
  "displayName": "Nguyễn Văn A"
}
```

| Field | Bắt buộc | Validation |
|---|:---:|---|
| `username` | Có | Không rỗng, tối đa 50 ký tự, không trùng |
| `password` | Có | Từ 6 đến 255 ký tự |
| `email` | Có | Email hợp lệ, tối đa 100 ký tự, không trùng |
| `displayName` | Không | Tối đa 100 ký tự; mặc định dùng username |

Response `201 Created`:

```json
{
  "status": 201,
  "message": "Đăng ký tài khoản thành công",
  "data": {
    "email": "student01@example.com",
    "verificationToken": "challenge-uuid"
  }
}
```

Tài khoản mới có trạng thái `UNVERIFIED`. Backend gửi OTP 6 chữ số tới email.
OTP mặc định có hiệu lực 5 phút.

Lỗi nghiệp vụ:

| Status | Trường hợp |
|---:|---|
| `409` | Username hoặc email đã được sử dụng |
| `429` | Vượt giới hạn đăng ký: mặc định 5 request/15 phút theo email |

### 2.2 Xác thực email đăng ký

```http
POST /api/auth/verify
```

Quyền: `Public`

Request:

```json
{
  "verificationToken": "challenge-uuid",
  "otp": "123456"
}
```

| Field | Validation |
|---|---|
| `verificationToken` | Không rỗng |
| `otp` | Đúng 6 chữ số |

Response `200 OK`: trả `AuthResponse` theo mục 2.5 và đăng nhập user ngay sau
khi xác thực thành công.

Lỗi nghiệp vụ:

| Status | Trường hợp |
|---:|---|
| `400` | Challenge không tồn tại, hết hạn, đã dùng hoặc sai mục đích |
| `401` | OTP sai ở lần 1 hoặc 2 |
| `403` | Tài khoản đã bị khóa |
| `429` | OTP sai lần thứ 3; challenge bị vô hiệu |

### 2.3 Gửi lại OTP đăng ký

```http
POST /api/auth/resend
```

Quyền: `Public`

Request:

```json
{
  "email": "student01@example.com"
}
```

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "email": "student01@example.com",
    "verificationToken": "new-challenge-uuid"
  }
}
```

- Cooldown giữa hai challenge cùng loại mặc định là 60 giây.
- Rate limit mặc định: 3 request/10 phút theo email.
- Endpoint trả phản hồi trung tính nếu tài khoản không tồn tại hoặc không còn
  ở trạng thái `UNVERIFIED`, nhằm hạn chế dò tài khoản.

### 2.4 Đăng nhập bằng username hoặc email

```http
POST /api/auth/login
```

Quyền: `Public`

Request khuyến nghị:

```json
{
  "identifier": "student01@example.com",
  "password": "secret123"
}
```

`identifier` chấp nhận username hoặc email, tự bỏ khoảng trắng đầu/cuối và
không phân biệt chữ hoa/thường. Để tương thích client cũ, hai dạng sau cũng hợp lệ:

```json
{ "username": "student01", "password": "secret123" }
```

```json
{ "email": "student01@example.com", "password": "secret123" }
```

| Field | Validation |
|---|---|
| `identifier` | Không rỗng, tối đa 100 ký tự |
| `password` | Từ 6 đến 255 ký tự |

Rate limit mặc định: 10 request/15 phút theo identifier đã chuẩn hóa.

Lỗi nghiệp vụ:

| Status | Trường hợp |
|---:|---|
| `401` | Username/email không tồn tại hoặc mật khẩu sai |
| `403` | Tài khoản chưa xác thực hoặc đã bị khóa |
| `429` | Vượt rate limit đăng nhập |

### 2.5 AuthResponse

Các endpoint login, verify, reset-password và refresh trả cấu trúc này:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "accessToken": "jwt-access-token",
    "tokenType": "Bearer",
    "expiresAt": "2026-08-11T20:15:00+07:00",
    "refreshToken": "opaque-refresh-token",
    "refreshTokenExpiresAt": "2026-09-10T20:00:00+07:00",
    "userId": "7a37c23a-14bb-4ed8-b4c9-4c028973a087",
    "username": "student01",
    "email": "student01@example.com",
    "displayName": "Nguyễn Văn A",
    "role": "USER",
    "status": "VERIFIED",
    "createdAt": "2026-08-11T19:00:00+07:00"
  }
}
```

### 2.6 Quên mật khẩu — yêu cầu OTP

```http
POST /api/auth/forgot-password
```

Quyền: `Public`

Request:

```json
{
  "email": "student01@example.com"
}
```

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "email": "student01@example.com",
    "resetToken": "reset-challenge-uuid"
  }
}
```

- Rate limit mặc định: 3 request/10 phút theo email.
- Response có chủ ý trung tính nếu email không tồn tại hoặc tài khoản bị khóa.

### 2.7 Quên mật khẩu — xác thực OTP

```http
POST /api/auth/verify-reset-otp
```

Quyền: `Public`

Request:

```json
{
  "resetToken": "reset-challenge-uuid",
  "otp": "123456"
}
```

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "email": "student01@example.com",
    "confirmedToken": "reset-challenge-uuid"
  }
}
```

Quy tắc lỗi OTP giống mục 2.2. `confirmedToken` chỉ dùng được một lần trong
thời hạn của challenge.

### 2.8 Quên mật khẩu — đặt mật khẩu mới

```http
POST /api/auth/reset-password
```

Quyền: `Public`

Request:

```json
{
  "confirmedToken": "reset-challenge-uuid",
  "newPassword": "newSecret123"
}
```

| Field | Validation |
|---|---|
| `confirmedToken` | Không rỗng, phải được xác thực và chưa sử dụng |
| `newPassword` | Từ 6 đến 255 ký tự |

Response `200 OK`: trả `AuthResponse`, đồng thời vô hiệu hóa toàn bộ phiên cũ.

### 2.9 Refresh token

```http
POST /api/auth/refresh
```

Quyền: `Public`, xác thực bằng refresh token trong body.

Request:

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Response `200 OK`: trả `AuthResponse` với access token và refresh token mới.
Refresh token trong request bị thu hồi ngay khi dùng thành công.

Lỗi `401 Unauthorized` khi token không tồn tại, hết hạn, đã dùng, đã bị thu
hồi, user không còn `VERIFIED`, hoặc tokenVersion không còn khớp.

### 2.10 Đăng xuất

```http
POST /api/auth/logout
```

Quyền: `Public`, thu hồi refresh token được gửi trong body.

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Response: `204 No Content`. Thao tác có tính idempotent đối với token không còn
tồn tại hoặc đã bị thu hồi.

## 3. User API — tài khoản hiện tại

### 3.1 Lấy hồ sơ hiện tại

```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

Quyền: `Authenticated`

Response `200 OK`: `ApiResponse<UserResponse>`.

### 3.2 Cập nhật hồ sơ hiện tại

```http
PUT /api/users/me
Authorization: Bearer <accessToken>
```

Request:

```json
{
  "displayName": "Nguyễn Văn B"
}
```

| Field | Validation |
|---|---|
| `displayName` | Bắt buộc, không rỗng, tối đa 100 ký tự |
| `email` | Không gửi; thay đổi email phải dùng luồng OTP tại mục 3.4 |

Response `200 OK`: `ApiResponse<UserResponse>` với message
`Cập nhật hồ sơ thành công`.

### 3.3 Đổi mật khẩu hiện tại

```http
PATCH /api/users/me/password
Authorization: Bearer <accessToken>
```

Request:

```json
{
  "currentPassword": "secret123",
  "newPassword": "newSecret123"
}
```

| Field | Validation |
|---|---|
| `currentPassword` | Bắt buộc và phải khớp mật khẩu hiện tại |
| `newPassword` | Bắt buộc, từ 6 đến 255 ký tự |

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Cập nhật mật khẩu thành công",
  "data": null
}
```

Sai mật khẩu hiện tại trả `401`. Đổi thành công sẽ vô hiệu hóa toàn bộ access
token và refresh token cũ, bao gồm token vừa dùng gọi endpoint này.

### 3.4 Đổi email — bước 1: xác thực email hiện tại

```http
POST /api/users/me/email-change
Authorization: Bearer <accessToken>
```

Không có request body. Backend gửi OTP tới email hiện tại.

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Đã gửi mã xác thực đến email hiện tại",
  "data": {
    "challengeId": "old-email-challenge-uuid",
    "email": "current@example.com"
  }
}
```

Rate limit mặc định: 3 request/10 phút theo userId. Cooldown OTP mặc định: 60 giây.

### 3.5 Đổi email — bước 2: xác thực email cũ và gửi OTP email mới

```http
POST /api/users/me/email-change/verify-old
Authorization: Bearer <accessToken>
```

Request:

```json
{
  "challengeId": "old-email-challenge-uuid",
  "otp": "123456",
  "newEmail": "new-email@example.com"
}
```

| Field | Validation |
|---|---|
| `challengeId` | Không rỗng, phải thuộc user hiện tại |
| `otp` | Đúng 6 chữ số |
| `newEmail` | Bắt buộc, email hợp lệ, tối đa 100 ký tự, chưa được sử dụng |

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Đã gửi mã xác thực đến email mới",
  "data": {
    "challengeId": "new-email-challenge-uuid",
    "email": "new-email@example.com"
  }
}
```

### 3.6 Đổi email — bước 3: xác nhận email mới

```http
POST /api/users/me/email-change/confirm
Authorization: Bearer <accessToken>
```

Request:

```json
{
  "challengeId": "new-email-challenge-uuid",
  "otp": "123456"
}
```

Response `200 OK`: `ApiResponse<UserResponse>` với email mới và message
`Thay đổi email thành công`.

Sau khi thành công, toàn bộ access/refresh token cũ bị vô hiệu hóa. Client phải
đăng nhập lại bằng username hoặc email mới.

## 4. User API — đăng ký môn học

Tất cả endpoint trong phần này yêu cầu access token hợp lệ.

### 4.1 Đăng ký môn học

```http
POST /api/users/me/course-enrollments/{subjectId}
Authorization: Bearer <accessToken>
```

Không có request body.

Response `201 Created`:

```json
{
  "status": 201,
  "message": "Đăng ký môn học thành công",
  "data": {
    "subjectId": "subject-uuid",
    "courseCode": "CT101",
    "name": "Lập trình căn bản",
    "description": "Mô tả môn học",
    "createdAt": "2026-08-01T08:00:00+07:00",
    "displayOrder": 1,
    "enrolledAt": "2026-08-11T20:00:00+07:00"
  }
}
```

| Status | Trường hợp |
|---:|---|
| `404` | Môn không tồn tại hoặc đang bị ẩn đối với user thường |
| `409` | User đã đăng ký môn này |

### 4.2 Hủy đăng ký môn học

```http
DELETE /api/users/me/course-enrollments/{subjectId}
Authorization: Bearer <accessToken>
```

Response: `204 No Content`.

Nếu user chưa đăng ký môn này, backend trả `404`.

Sau khi hủy, user không thể truy cập chương, lý thuyết, câu hỏi, danh sách đề,
bắt đầu hoặc tiếp tục làm quiz thuộc môn đó. Backend trả `403` cho các trường hợp này.

### 4.3 Lấy danh sách môn đã đăng ký

```http
GET /api/users/me/course-enrollments
Authorization: Bearer <accessToken>
```

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "subjectId": "subject-uuid",
      "courseCode": "CT101",
      "name": "Lập trình căn bản",
      "description": "Mô tả môn học",
      "createdAt": "2026-08-01T08:00:00+07:00",
      "displayOrder": 1,
      "enrolledAt": "2026-08-11T20:00:00+07:00"
    }
  ]
}
```

Danh sách được sắp xếp theo `displayOrder`, `createdAt`, rồi `subjectId`. Môn
đang bị ẩn không xuất hiện với user thường.

## 5. User API — quản trị

Tất cả endpoint trong phần này yêu cầu role `ADMINISTRATOR`.

### 5.1 Danh sách user có phân trang

```http
GET /api/users?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <adminAccessToken>
```

Query params:

| Param | Mặc định | Ghi chú |
|---|---|---|
| `page` | `0` | Đánh số từ 0 |
| `size` | `20` | Tối đa 100 |
| `sort` | `createdAt,desc` | Có thể truyền nhiều tham số `sort` |

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [],
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

Mỗi phần tử `content` có cấu trúc `UserResponse`.

### 5.2 Lấy chi tiết user

```http
GET /api/users/{userId}
Authorization: Bearer <adminAccessToken>
```

Response `200 OK`: `ApiResponse<UserResponse>`. User không tồn tại trả `404`.

### 5.3 Cập nhật hồ sơ user

```http
PUT /api/users/{userId}
Authorization: Bearer <adminAccessToken>
```

Request:

```json
{
  "displayName": "Tên hiển thị mới"
}
```

Response `200 OK`: `ApiResponse<UserResponse>` với message
`Cập nhật người dùng thành công`.

Email không được thay đổi qua endpoint này.

### 5.4 Admin đặt lại mật khẩu user

```http
PATCH /api/users/{userId}/password
Authorization: Bearer <adminAccessToken>
```

Request:

```json
{
  "newPassword": "newSecret123"
}
```

Response `200 OK`:

```json
{
  "status": 200,
  "message": "Cập nhật mật khẩu người dùng thành công",
  "data": null
}
```

Toàn bộ phiên cũ của user mục tiêu bị vô hiệu hóa.

### 5.5 Xóa user

```http
DELETE /api/users/{userId}
Authorization: Bearer <adminAccessToken>
```

Response: `204 No Content`.

Backend thực hiện xóa mềm và ẩn danh tài khoản, đồng thời giữ lại dữ liệu học
tập liên quan. Admin không được tự xóa tài khoản đang đăng nhập (`400`).

### 5.6 Cập nhật trạng thái user

```http
PATCH /api/users/{userId}/status
Authorization: Bearer <adminAccessToken>
```

Request:

```json
{
  "status": "BANNED"
}
```

Giá trị hợp lệ: `UNVERIFIED`, `VERIFIED`, `BANNED`.

Response `200 OK`: `ApiResponse<UserResponse>` với message
`Cập nhật trạng thái người dùng thành công`.

Thay đổi trạng thái làm vô hiệu hóa toàn bộ phiên cũ. Admin không được thay đổi
trạng thái của chính tài khoản đang đăng nhập (`400`).

### 5.7 Cập nhật role user

```http
PATCH /api/users/{userId}/role
Authorization: Bearer <adminAccessToken>
```

Request:

```json
{
  "role": "ADMINISTRATOR"
}
```

Giá trị hợp lệ: `USER`, `ADMINISTRATOR`.

Response `200 OK`: `ApiResponse<UserResponse>` với message
`Cập nhật vai trò người dùng thành công`.

Thay đổi role làm vô hiệu hóa toàn bộ phiên cũ. Admin không được đổi role của
chính tài khoản đang đăng nhập (`400`).

## 6. Luồng tích hợp khuyến nghị

### Đăng ký và xác thực

```text
POST /auth/register
  -> nhận verificationToken
  -> nhập OTP từ email
POST /auth/verify
  -> nhận accessToken + refreshToken
```

### Làm mới phiên

```text
POST /auth/refresh với refreshToken hiện tại
  -> lưu accessToken mới
  -> thay refreshToken cũ bằng refreshToken mới
```

Client không được gửi lại refresh token cũ sau khi refresh thành công.

### Đổi email

```text
POST /users/me/email-change
  -> OTP email hiện tại
POST /users/me/email-change/verify-old
  -> OTP email mới
POST /users/me/email-change/confirm
  -> email được cập nhật, token cũ bị thu hồi
  -> đăng nhập lại
```

### Xử lý access token hết hạn

```text
API trả 401
  -> gọi POST /auth/refresh đúng một lần
  -> cập nhật cả accessToken và refreshToken
  -> retry request ban đầu đúng một lần
  -> nếu refresh trả 401: xóa phiên local và chuyển về màn hình đăng nhập
```

