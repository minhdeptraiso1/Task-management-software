# 08 — API Reference

**Base URL:** `http://localhost:8080/api`
**Swagger UI:** `http://localhost:8080/api/swagger`
**OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`

---

## Authentication

Các API yêu cầu xác thực phải gửi header:

```
Authorization: Bearer <access_token>
```

---

## Auth APIs

### POST `/auth/login`

Đăng nhập, nhận access token và refresh token.

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "123456"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  },
  "error": null
}
```

**Lỗi có thể xảy ra:**

| HTTP | ErrorCode | Mô tả |
|------|-----------|-------|
| 400 | 400003 | Email/password để trống |
| 401 | 401001 | Sai email hoặc mật khẩu |
| 403 | 403002 | Tài khoản bị khóa |
| 429 | 429001 | Đăng nhập sai quá 5 lần/phút |

---

### POST `/auth/refresh`

Làm mới access token từ refresh token còn hiệu lực.

**Query param:** `?refreshToken=<refresh_token>`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...(mới)",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(cũ, không đổi)"
  },
  "error": null
}
```

**Lỗi có thể xảy ra:**

| HTTP | ErrorCode | Mô tả |
|------|-----------|-------|
| 401 | 401002 | Refresh token hết hạn |
| 401 | 401003 | Refresh token đã bị thu hồi |

---

### POST `/auth/logout`

Đăng xuất. Access token bị blacklist, refresh token bị revoke.

**Headers:** `Authorization: Bearer <access_token>`
**Query param:** `?refreshToken=<refresh_token>`

**Response 200:**
```json
{
  "success": true,
  "data": null,
  "error": null
}
```

---

## User APIs

> Tất cả yêu cầu header `Authorization: Bearer <access_token>`

---

### GET `/users/me`

Lấy thông tin user đang đăng nhập.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "email": "admin@example.com",
    "enabled": true,
    "role": "ADMIN"
  },
  "error": null
}
```

---

### GET `/users/search`

Tìm kiếm user theo keyword, role, trạng thái. **Chỉ ADMIN.**

**Query params:**

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `keyword` | String | No | Tìm theo username hoặc email |
| `role` | `ADMIN` \| `USER` | No | Filter theo role |
| `enabled` | Boolean | No | Filter theo trạng thái |
| `page` | int | No | Trang (default 0) |
| `size` | int | No | Số lượng / trang (default 20) |
| `sort` | String | No | VD: `username,asc` |

**Ví dụ:** `GET /users/search?keyword=admin&role=ADMIN&page=0&size=10`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "admin",
        "email": "admin@example.com",
        "enabled": true,
        "role": "ADMIN"
      }
    ],
    "pageable": { ... },
    "totalElements": 1,
    "totalPages": 1
  },
  "error": null
}
```

---

### POST `/users`

Tạo user mới. **Chỉ ADMIN.**

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "123456",
  "role": "USER"
}
```

**Validation:**
- `username`: bắt buộc, không được trống
- `email`: bắt buộc, phải đúng format email
- `password`: bắt buộc
- `role`: bắt buộc, phải là `ADMIN` hoặc `USER`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": "...",
    "username": "newuser",
    "email": "newuser@example.com",
    "enabled": true,
    "role": "USER"
  },
  "error": null
}
```

**Lỗi có thể xảy ra:**

| HTTP | ErrorCode | Mô tả |
|------|-----------|-------|
| 400 | 400003 | Validation fail |
| 409 | 409001 | Username đã tồn tại |
| 409 | 409002 | Email đã tồn tại |

---

### PUT `/users/{id}`

Cập nhật user. **Chỉ ADMIN.**

**Path param:** `id` — UUID của user

**Request Body:** (tất cả optional — chỉ gửi field cần sửa)
```json
{
  "password": "newpassword",
  "role": "ADMIN",
  "enabled": false
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": "...",
    "username": "admin",
    "email": "admin@example.com",
    "enabled": false,
    "role": "ADMIN"
  },
  "error": null
}
```

---

### DELETE `/users/{id}`

Xóa mềm user. **Chỉ ADMIN.**

**Path param:** `id` — UUID của user

**Response 200:**
```json
{
  "success": true,
  "data": null,
  "error": null
}
```

---

## WebSocket

**Endpoint:** `ws://localhost:8080/api/ws` (SockJS)
**Subscribe:** `/topic/audit-logs`

Mỗi khi có action (login/logout), server push message:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "action": "LOGIN",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Ví dụ kết nối (JavaScript):**
```javascript
const socket = new SockJS('/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/audit-logs', (message) => {
    const log = JSON.parse(message.body);
    console.log(log);
  });
});
```

---

## Error Response Format (Chung)

Mọi lỗi đều trả theo cấu trúc:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": 404001,
    "message": "Không tìm thấy người dùng"
  }
}
```

Xem đầy đủ danh sách error codes tại [05_error_handling.md](./05_error_handling.md).
