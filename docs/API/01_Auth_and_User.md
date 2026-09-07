# 🔐 Module 1: Xác thực, Người dùng & Phân quyền (Auth & User API Specification)

Tài liệu chi tiết các endpoint thuộc phân hệ Xác thực (`/api/auth`), Quản lý Hồ sơ & Người dùng (`/api/users`), Vai trò (`/api/roles`), Quyền hạn (`/api/permissions`) và Thống kê Quản trị (`/api/admin/data-stats`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Đăng ký tài khoản mới (trả về `verificationToken` & gửi OTP). |
| `POST` | `/api/auth/verify` | Public | Xác thực OTP đăng ký (kích hoạt `VERIFIED` & cấp Access Token). |
| `POST` | `/api/auth/resend` | Public | Gửi lại mã OTP kích hoạt tài khoản. |
| `POST` | `/api/auth/login` | Public | Đăng nhập hệ thống (bằng username hoặc email). |
| `POST` | `/api/auth/forgot-password` | Public | Yêu cầu khôi phục mật khẩu (gửi OTP về email). |
| `POST` | `/api/auth/verify-reset-otp` | Public | Xác thực OTP khôi phục mật khẩu (nhận `resetToken`). |
| `POST` | `/api/auth/reset-password` | Public | Đặt lại mật khẩu mới bằng `resetToken`. |
| `POST` | `/api/auth/refresh` | Public | Cấp mới Access Token bằng Refresh Token. |
| `POST` | `/api/auth/logout` | Public / User | Đăng xuất và hủy Refresh Token. |
| `GET` | `/api/users/me` | `isAuthenticated()` | Lấy thông tin hồ sơ của người dùng hiện tại. |
| `PUT` | `/api/users/me` | `isAuthenticated()` | Cập nhật thông tin hồ sơ (tên hiển thị, avatar). |
| `PATCH` | `/api/users/me/password` | `isAuthenticated()` | Người dùng tự đổi mật khẩu cá nhân. |
| `POST` | `/api/users/me/email-change` | `isAuthenticated()` | Bước 1: Yêu cầu đổi email (gửi OTP về email cũ). |
| `POST` | `/api/users/me/email-change/verify-old` | `isAuthenticated()` | Bước 2: Xác thực OTP email cũ & gửi OTP tới email mới. |
| `POST` | `/api/users/me/email-change/confirm` | `isAuthenticated()` | Bước 3: Xác thực OTP email mới và hoàn tất cập nhật. |
| `GET` | `/api/users` | `ADMINISTRATOR` | Danh sách người dùng hệ thống (có phân trang & sắp xếp). |
| `GET` | `/api/users/{userId}` | `ADMINISTRATOR` | Lấy thông tin chi tiết một người dùng bất kỳ. |
| `PUT` | `/api/users/{userId}` | `ADMINISTRATOR` | Cập nhật thông tin hồ sơ người dùng bất kỳ. |
| `PATCH` | `/api/users/{userId}/password` | `ADMINISTRATOR` | Admin đặt lại mật khẩu cho người dùng. |
| `PATCH` | `/api/users/{userId}/status` | `ADMINISTRATOR` | Khóa / Mở khóa tài khoản (`VERIFIED` / `BANNED`). |
| `PATCH` | `/api/users/{userId}/role` | `ADMINISTRATOR` | Thay đổi vai trò người dùng (`USER` / `ADMINISTRATOR`). |
| `DELETE` | `/api/users/{userId}` | `ADMINISTRATOR` | Xóa mềm người dùng (Hệ thống chỉ hỗ trợ xóa mềm). |
| `GET` | `/api/roles` | `ADMINISTRATOR` | Lấy danh sách tất cả các vai trò trong hệ thống. |
| `DELETE` | `/api/roles/{roleId}` | `ADMINISTRATOR` | Xóa mềm một vai trò (Hệ thống chỉ hỗ trợ xóa mềm). |
| `GET` | `/api/permissions` | `ADMINISTRATOR` | Lấy danh sách tất cả các quyền hạn trong hệ thống. |
| `DELETE` | `/api/permissions/{permissionId}` | `ADMINISTRATOR` | Xóa mềm một quyền hạn (Hệ thống chỉ hỗ trợ xóa mềm). |
| `GET` | `/api/admin/data-stats` | `ADMINISTRATOR` | Thống kê số lượng users, subjects, questions, quizzes, attempts. |

---

## 2. 🔐 Chi tiết Endpoints Xác thực (`/api/auth`)

### 2.1 Đăng ký tài khoản (`POST /api/auth/register`)
- **Usecase:** Người dùng mới đăng ký tài khoản. Hệ thống tạo tài khoản ở trạng thái `UNVERIFIED`, tạo thử thách xác thực (Challenge OTP 6 số hết hạn sau 5 phút) và gửi email kích hoạt.
- **Quyền:** Public.

#### Request Body:
```json
{
  "username": "nguyenhuan2005",
  "password": "Password@123",
  "email": "huan.nguyen@example.com",
  "displayName": "Nguyễn Hoàng Huân"
}
```
*Ghi chú:*
- `username`: Bắt buộc, 3-50 ký tự, không chứa ký tự đặc biệt.
- `password`: Bắt buộc, 6-255 ký tự.
- `email`: Bắt buộc, đúng định dạng email, tối đa 100 ký tự.
- `displayName`: Không bắt buộc, tối đa 100 ký tự.

#### Response `201 Created`:
```json
{
  "status": 201,
  "message": "Đăng ký tài khoản thành công",
  "data": {
    "email": "huan.nguyen@example.com",
    "verificationToken": "e7b8c3f4-5678-4a12-98bc-abcdef012345"
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Tên đăng nhập đã được sử dụng`, `Email đã được đăng ký`, `Mật khẩu phải từ 6 ký tự trở lên`.
- `429 Too Many Requests`: `Bạn đã yêu cầu gửi mã quá nhiều lần. Vui lòng thử lại sau ít phút.`

---

### 2.2 Xác thực OTP đăng ký (`POST /api/auth/verify`)
- **Usecase:** Người dùng nhập mã OTP nhận được từ email cùng `verificationToken` để hoàn tất kích hoạt. Khi thành công, tài khoản chuyển sang `VERIFIED` và trả về ngay bộ Access Token & Refresh Token.
- **Quyền:** Public.

#### Request Body:
```json
{
  "verificationToken": "e7b8c3f4-5678-4a12-98bc-abcdef012345",
  "otp": "123456"
}
```

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Xác thực tài khoản thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7d9a1f2e-4b6c-8e0a-...",
    "user": {
      "userId": "usr_99a8b7c6",
      "username": "nguyenhuan2005",
      "email": "huan.nguyen@example.com",
      "displayName": "Nguyễn Hoàng Huân",
      "role": "USER",
      "status": "VERIFIED",
      "avatarUrl": null,
      "createdAt": "2026-08-21T10:00:00Z"
    }
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Mã OTP không chính xác. Bạn còn 2 lần thử.`, `Mã OTP đã hết hạn hoặc không tồn tại.`, `Thử thách xác thực đã bị vô hiệu do nhập sai quá số lần quy định.`

---

### 2.3 Gửi lại OTP kích hoạt (`POST /api/auth/resend`)
- **Usecase:** Người dùng yêu cầu gửi lại OTP khi mã cũ hết hạn hoặc không nhận được thư.
- **Quyền:** Public.

#### Request Body:
```json
{
  "email": "huan.nguyen@example.com"
}
```

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Đã gửi lại mã xác thực đến email của bạn",
  "data": {
    "email": "huan.nguyen@example.com",
    "verificationToken": "8f3b21c4-9876-4d32-bbef-123456abcdef"
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Tài khoản đã được xác thực trước đó.`, `Không tìm thấy tài khoản với email này.`
- `429 Too Many Requests`: Gửi yêu cầu quá dày đặc trong 60 giây.

---

### 2.4 Đăng nhập hệ thống (`POST /api/auth/login`)
- **Usecase:** Đăng nhập bằng `username` hoặc `email` kèm mật khẩu.
- **Quyền:** Public.

#### Request Body:
```json
{
  "identifier": "nguyenhuan2005",
  "password": "Password@123"
}
```

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7d9a1f2e-4b6c-8e0a-...",
    "user": {
      "userId": "usr_99a8b7c6",
      "username": "nguyenhuan2005",
      "email": "huan.nguyen@example.com",
      "displayName": "Nguyễn Hoàng Huân",
      "role": "USER",
      "status": "VERIFIED",
      "avatarUrl": null,
      "createdAt": "2026-08-21T10:00:00Z"
    }
  }
}
```

#### Các lỗi có thể gặp:
- `400 Bad Request`: `Tên đăng nhập hoặc mật khẩu không chính xác.`
- `403 Forbidden`: `Tài khoản chưa được kích hoạt. Vui lòng xác thực email trước khi đăng nhập.`, `Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.`

---

### 2.5 Quên mật khẩu & Đặt lại mật khẩu (Quy trình 3 bước)

#### Bước 1: Yêu cầu khôi phục (`POST /api/auth/forgot-password`)
- **Request Body:**
  ```json
  { "email": "huan.nguyen@example.com" }
  ```
- **Response `200 OK`:**
  ```json
  {
    "status": 200,
    "message": "Mã xác thực đã được gửi đến email của bạn",
    "data": {
      "email": "huan.nguyen@example.com",
      "resetChallengeToken": "chk_123456789"
    }
  }
  ```

#### Bước 2: Xác thực OTP khôi phục (`POST /api/auth/verify-reset-otp`)
- **Request Body:**
  ```json
  {
    "resetChallengeToken": "chk_123456789",
    "otp": "654321"
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "status": 200,
    "message": "Xác thực OTP thành công",
    "data": {
      "resetToken": "rst_abcdef987654"
    }
  }
  ```

#### Bước 3: Đặt mật khẩu mới (`POST /api/auth/reset-password`)
- **Request Body:**
  ```json
  {
    "resetToken": "rst_abcdef987654",
    "newPassword": "NewSecurePassword@2026"
  }
  ```
- **Response `200 OK`:** Trả về `AuthResponse` (đăng nhập tự động ngay sau khi đổi mật khẩu).

---

### 2.6 Làm mới Token (`POST /api/auth/refresh`) & Đăng xuất (`POST /api/auth/logout`)

#### Làm mới Token:
- **Request Body:** `{ "refreshToken": "7d9a1f2e-..." }`
- **Response `200 OK`:** Trả về `AuthResponse` chứa cặp `accessToken` và `refreshToken` mới (xoay vòng token an toàn - Refresh Token Rotation).
- **Lỗi 401:** `Refresh token không hợp lệ hoặc đã hết hạn.`

#### Đăng xuất:
- **Request Body:** `{ "refreshToken": "7d9a1f2e-..." }`
- **Response `204 No Content`:** Thu hồi phiên đăng nhập hiện tại và xóa token khỏi cơ sở dữ liệu.

---

## 3. 👤 Chi tiết Endpoints Quản lý Người dùng (`/api/users`)

### 3.1 Lấy & Cập nhật Hồ sơ cá nhân (`GET / PUT /api/users/me`)
- **Header:** `Authorization: Bearer <accessToken>`
- **`GET /api/users/me`:** Trả về thông tin chi tiết của người dùng đang đăng nhập (`UserResponse`).
- **`PUT /api/users/me`:**
  - **Request Body:**
    ```json
    {
      "displayName": "Nguyễn Hoàng Huân (Cập nhật)",
      "avatarUrl": "http://localhost:8080/api/uploads/avatars/user-123.png"
    }
    ```
  - **Response `200 OK`:** Trả về `UserResponse` đã cập nhật.

---

### 3.2 Đổi mật khẩu cá nhân (`PATCH /api/users/me/password`)
- **Header:** `Authorization: Bearer <accessToken>`
- **Request Body:**
  ```json
  {
    "currentPassword": "Password@123",
    "newPassword": "MyNewPassword@456"
  }
  ```
- **Response `200 OK`:** `{ "status": 200, "message": "Cập nhật mật khẩu thành công", "data": null }`
- **Lỗi 400:** `Mật khẩu hiện tại không chính xác.`, `Mật khẩu mới không được trùng với mật khẩu cũ.`

---

### 3.3 Đổi Email an toàn 2 bước (`/api/users/me/email-change/...`)

1. **Bước 1: Gửi OTP tới email cũ (`POST /api/users/me/email-change`)**
   - Không cần body. Backend gửi OTP đến email hiện tại của tài khoản.
   - Trả về `{ "challengeToken": "emc_111...", "step": "OLD_EMAIL_VERIFICATION" }`.
2. **Bước 2: Xác thực email cũ & Cung cấp email mới (`POST /api/users/me/email-change/verify-old`)**
   - Request Body:
     ```json
     {
       "challengeToken": "emc_111...",
       "otp": "123456",
       "newEmail": "new.email@example.com"
     }
     ```
   - Backend kiểm tra nếu `newEmail` chưa tồn tại trong hệ thống thì sẽ gửi OTP tới `newEmail`.
   - Trả về `{ "challengeToken": "emc_222...", "step": "NEW_EMAIL_VERIFICATION" }`.
3. **Bước 3: Xác thực email mới & Cập nhật (`POST /api/users/me/email-change/confirm`)**
   - Request Body:
     ```json
     {
       "challengeToken": "emc_222...",
       "otp": "654321"
     }
     ```
   - Trả về `UserResponse` với email đã đổi thành công.

---

### 3.4 Quản trị viên quản lý Người dùng (`ADMINISTRATOR`)

#### Danh sách người dùng (`GET /api/users`)
- **Query Params:**
  - `page`: Số trang (0-indexed, mặc định: `0`).
  - `size`: Số bản ghi mỗi trang (mặc định: `20`, tối đa: `100`).
  - `sort`: Tiêu chí sắp xếp (ví dụ: `createdAt,desc` hoặc `username,asc`).
- **Response `200 OK`:** `PageResponse<UserResponse>`.

#### Khóa / Mở khóa tài khoản (`PATCH /api/users/{userId}/status`)
- **Request Body:** `{ "status": "BANNED" }` hoặc `{ "status": "VERIFIED" }`
- **Lỗi 400:** `Admin không thể tự khóa tài khoản của chính mình.`

#### Thay đổi Vai trò (`PATCH /api/users/{userId}/role`)
- **Request Body:** `{ "role": "ADMINISTRATOR" }` hoặc `{ "role": "USER" }`
- **Lỗi 400:** `Admin không thể tự hạ quyền của chính mình.`

#### Admin đặt lại mật khẩu (`PATCH /api/users/{userId}/password`)
- **Request Body:** `{ "newPassword": "NewAdminAssignedPassword@123" }`

#### Xóa Người dùng (`DELETE /api/users/{userId}`)
- **Query Params:**
  - `hard`: `false` (mặc định - Xóa mềm: ẩn danh dữ liệu, giữ lại lịch sử học tập. Hệ thống không cho phép xóa cứng).
- **Quy tắc bảo vệ:** Admin không thể tự xóa tài khoản của chính mình (`400 Bad Request`).
- **Response `204 No Content`**.

---

## 4. 🛡️ Quản lý Vai trò & Quyền hạn (`/api/roles`, `/api/permissions`)

### 4.1 Danh sách Vai trò (`GET /api/roles`)
- **Quyền:** `ADMINISTRATOR`
- **Response `200 OK`:**
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    { "roleId": "role_user", "name": "USER", "deletedAt": null },
    { "roleId": "role_admin", "name": "ADMINISTRATOR", "deletedAt": null }
  ]
}
```

### 4.2 Xóa Vai trò (`DELETE /api/roles/{roleId}`)
- **Query Params:** `?hard=true` (xóa cứng) hoặc `?hard=false` (xóa mềm).
- **Response `204 No Content`**.

### 4.3 Danh sách Quyền hạn (`GET /api/permissions`)
- **Quyền:** `ADMINISTRATOR`
- **Response `200 OK`:** Danh sách permissions trong hệ thống.

### 4.4 Xóa Quyền hạn (`DELETE /api/permissions/{permissionId}`)
- **Query Params:** `?hard=true` (xóa cứng) hoặc `?hard=false` (xóa mềm).
- **Response `204 No Content`**.

---

## 5. 📊 Thống kê Hệ thống (`GET /api/admin/data-stats`)
- **Usecase:** Dùng cho bảng tin Admin Dashboard hiển thị số lượng thực thể đang hoạt động. Tự động loại bỏ các bản ghi đã xóa mềm (`deleted_at IS NOT NULL`).
- **Quyền:** `ADMINISTRATOR`

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "totalUsers": 1250,
    "totalSubjects": 12,
    "totalQuestions": 3480,
    "totalQuizzes": 45,
    "totalCompletedAttempts": 8920
  }
}
```
