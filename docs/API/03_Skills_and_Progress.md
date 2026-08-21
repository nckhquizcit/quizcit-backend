# 🎯 Module 3: Kỹ năng & Tiến độ Học tập (Skills & Progress API Specification)

Tài liệu chi tiết các endpoint Quản lý Kỹ năng (`/api/skills`), Nhập kỹ năng hàng loạt từ file CSV (`/api/skills/bulk`) và Theo dõi Năng lực / Tiến độ Học tập của Học viên theo mô hình IRT (`/api/users/{userId}/skills`, `/api/users/{userId}/abilities`).

---

## 1. 📋 Bảng tổng hợp Endpoints

| Phương thức | Endpoint | Phân quyền | Mô tả chức năng |
|---|---|---|---|
| `GET` | `/api/skills` | Public / User | Lấy danh sách tất cả các kỹ năng trong hệ thống. |
| `GET` | `/api/skills/{skillId}` | Public / User | Lấy chi tiết thông tin một kỹ năng. |
| `POST` | `/api/skills` | `ADMINISTRATOR` | Tạo mới một kỹ năng. |
| `POST` | `/api/skills/bulk` | `ADMINISTRATOR` | Nhập hàng loạt kỹ năng (từ file CSV hoặc danh sách import). |
| `PUT` | `/api/skills/{skillId}` | `ADMINISTRATOR` | Cập nhật thông tin kỹ năng (tên, mô tả). |
| `DELETE` | `/api/skills/{skillId}` | `ADMINISTRATOR` | Xóa mềm hoặc Xóa cứng (`?hard=true`) kỹ năng. |
| `GET` | `/api/users/{userId}/skills` | Owner / `ADMINISTRATOR` | Lấy danh sách điểm năng lực ($\theta$) của học viên theo từng kỹ năng. |
| `GET` | `/api/users/{userId}/skills/{skillId}` | Owner / `ADMINISTRATOR` | Lấy điểm năng lực của học viên cho một kỹ năng cụ thể. |
| `GET` | `/api/users/{userId}/abilities/history` | Owner / `ADMINISTRATOR` | Lấy toàn bộ lịch sử biến thiên năng lực qua các lần làm bài thi. |
| `GET` | `/api/users/{userId}/skills/{skillId}/history` | Owner / `ADMINISTRATOR` | Lấy lịch sử biến thiên năng lực của một kỹ năng cụ thể. |
| `GET` | `/api/users/{userId}/abilities/average` | Owner / `ADMINISTRATOR` | Lấy điểm năng lực trung bình ($\bar{\theta}$) tổng thể của học viên. |

---

## 2. 🧠 Chi tiết Quản lý Kỹ năng (`/api/skills`)

### 2.1 Lấy danh sách kỹ năng (`GET /api/skills`)
- **Usecase:** Dùng để hiển thị danh sách kỹ năng khi biên soạn câu hỏi, gắn kỹ năng cho chương học, hoặc vẽ biểu đồ Radar năng lực trên Frontend.
- **Quyền:** Public / User.

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "skillId": "skl_pointer_arithmetic",
      "name": "Toán tử Con trỏ",
      "description": "Hiểu và thực hiện các phép cộng, trừ con trỏ và duyệt mảng qua con trỏ"
    },
    {
      "skillId": "skl_dynamic_memory",
      "name": "Cấp phát bộ nhớ động",
      "description": "Sử dụng thành thạo malloc, calloc, realloc và free"
    }
  ]
}
```

---

### 2.2 Tạo & Cập nhật Kỹ năng (`POST / PUT`)
- **Tạo:** `POST /api/skills`
- **Sửa:** `PUT /api/skills/{skillId}`
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "name": "Đệ quy và Quy hoạch động",
  "description": "Xác định bài toán con gối nhau và cấu trúc con tối ưu"
}
```
*Ghi chú:*
- `name`: Bắt buộc, 2-100 ký tự, không được trùng với kỹ năng đang hoạt động khác.
- `description`: Không bắt buộc, tối đa 500 ký tự.

---

### 2.3 Nhập kỹ năng hàng loạt từ file CSV (`POST /api/skills/bulk`)
- **Usecase:** Quản trị viên tải lên file CSV chứa danh sách nhiều kỹ năng cùng lúc (sau khi đã xem trước bản preview trên giao diện).
- **Quyền:** `ADMINISTRATOR`

#### Request Body:
```json
{
  "items": [
    {
      "clientId": "row-1",
      "skill": {
        "name": "Con trỏ hàm (Function Pointer)",
        "description": "Sử dụng con trỏ trỏ tới hàm và hàm callback trong C"
      }
    },
    {
      "clientId": "row-2",
      "skill": {
        "name": "Cấu trúc dữ liệu Tự trỏ",
        "description": "Cấu trúc Struct tự liên kết để xây dựng Danh sách liên kết"
      }
    }
  ]
}
```

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Đã xử lý import kỹ năng hàng loạt",
  "data": {
    "total": 2,
    "successCount": 2,
    "failureCount": 0,
    "results": [
      {
        "clientId": "row-1",
        "success": true,
        "skill": {
          "skillId": "skl_func_pointer",
          "name": "Con trỏ hàm (Function Pointer)",
          "description": "Sử dụng con trỏ trỏ tới hàm và hàm callback trong C"
        },
        "errorMessage": null
      },
      {
        "clientId": "row-2",
        "success": true,
        "skill": {
          "skillId": "skl_self_ref_struct",
          "name": "Cấu trúc dữ liệu Tự trỏ",
          "description": "Cấu trúc Struct tự liên kết để xây dựng Danh sách liên kết"
        },
        "errorMessage": null
      }
    ]
  }
}
```

#### Xử lý lỗi từng dòng (Atomic per-item):
Nếu có một số dòng bị trùng tên hoặc sai độ dài, API vẫn tiếp tục xử lý các dòng hợp lệ còn lại và trả về `success: false` kèm `errorMessage` chi tiết tương ứng với `clientId` của dòng bị lỗi.

---

### 2.4 Xóa Kỹ năng (`DELETE /api/skills/{skillId}`)
- **Query Params:**
  - `hard`: `false` (mặc định - Xóa mềm kỹ năng).
  - `hard`: `true` (Xóa cứng: dọn sạch liên kết trong `question_skills`, `chapter_skills`, `user_skill_ability` và `ability_history`).
- **Response `204 No Content`**.

---

## 3. 📈 Chi tiết Theo dõi Năng lực & Tiến độ (`/api/users/...`)

> **Quy tắc phân quyền:** Học viên chỉ có thể xem dữ liệu của chính mình (`userId == principal.userId`). Quản trị viên (`ADMINISTRATOR`) có quyền xem dữ liệu của bất kỳ học viên nào.

### 3.1 Bảng điểm Năng lực IRT theo Kỹ năng (`GET /api/users/{userId}/skills`)
- **Usecase:** Vẽ biểu đồ Radar/Spider chart hoặc bảng đánh giá điểm mạnh / điểm yếu của học viên.
- **Thang đo năng lực IRT:** Điểm $\theta$ nằm trong khoảng `[-4.0, +4.0]` (Mặc định khi bắt đầu: `0.0` - Mức trung bình).

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "skillId": "skl_pointer_arithmetic",
      "skillName": "Toán tử Con trỏ",
      "theta": 1.45,
      "confidence": 0.85,
      "updatedAt": "2026-08-21T09:15:00Z"
    },
    {
      "skillId": "skl_dynamic_memory",
      "skillName": "Cấp phát bộ nhớ động",
      "theta": -0.60,
      "confidence": 0.72,
      "updatedAt": "2026-08-21T09:15:00Z"
    }
  ]
}
```

---

### 3.2 Lịch sử Biến thiên Năng lực (`GET /api/users/{userId}/abilities/history`)
- **Usecase:** Vẽ biểu đồ đường (Line chart) theo dõi sự tiến bộ của học viên qua từng bài kiểm tra.

#### Response `200 OK`:
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "historyId": "abh_001",
      "attemptId": "att_test_123",
      "skillId": "skl_pointer_arithmetic",
      "skillName": "Toán tử Con trỏ",
      "oldTheta": 1.10,
      "newTheta": 1.45,
      "thetaChange": 0.35,
      "createdAt": "2026-08-21T09:15:00Z"
    },
    {
      "historyId": "abh_002",
      "attemptId": "att_test_123",
      "skillId": "skl_dynamic_memory",
      "skillName": "Cấp phát bộ nhớ động",
      "oldTheta": -0.80,
      "newTheta": -0.60,
      "thetaChange": 0.20,
      "createdAt": "2026-08-21T09:15:00Z"
    }
  ]
}
```

---

### 3.3 Điểm Năng lực Trung bình Tổng thể (`GET /api/users/{userId}/abilities/average`)
- **Usecase:** Hiển thị chỉ số đánh giá tổng quát của học viên trên Dashboard cá nhân.
- **Response `200 OK`:**
```json
{
  "status": 200,
  "message": "Success",
  "data": 0.85
}
```
