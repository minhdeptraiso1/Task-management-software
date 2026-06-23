# 04 — Security Architecture

## Tổng Quan Flow Bảo Mật

```
Client Request
     │
     ▼
┌─────────────────────────────┐
│   JwtAuthenticationFilter   │  ← Chạy TRƯỚC mọi request
│                             │
│  1. Bỏ qua nếu là URL public│
│  2. Đọc token từ header     │
│  3. Kiểm tra blacklist Redis│  ← Token bị thu hồi?
│  4. Validate JWT signature  │  ← Token hợp lệ?
│  5. Set SecurityContext     │
└─────────────────────────────┘
     │
     ▼
┌─────────────────────────────┐
│   SecurityFilterChain       │
│                             │
│  - Public URLs: permitAll   │
│  - Khác: authenticated()    │
│  - Method-level: @PreAuthorize
└─────────────────────────────┘
     │
     ▼
  Controller → Service
```

---

## JWT Token

### Cấu Trúc Access Token (Payload)

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",  // userId
  "username": "admin",
  "role": "ADMIN",
  "iat": 1716123456,
  "exp": 1716124356
}
```

### Cấu Trúc Refresh Token (Payload)

```json
{
  "sub": "session-uuid",   // sessionId trong bảng token_sessions
  "iat": 1716123456
  // Không có exp trong JWT — kiểm tra qua DB (expired_at column)
}
```

### Sinh Token

```java
// JwtTokenProvider.java

// Access token — có đầy đủ claims, hết hạn sau N giây (config)
public String generateAccessToken(UUID userId, String username, String role) { ... }

// Refresh token — chỉ chứa sessionId, không hết hạn JWT
// Hết hạn được quản lý bởi cột expired_at trong DB
public String generateRefreshToken(UUID sessionId) { ... }
```

---

## Authentication Flow

### 1. Login

```
POST /auth/login
│
├─ LoginRateLimiter.check(email)   ← Kiểm tra rate limit (Redis)
├─ AuthenticationManager.authenticate()  ← Xác thực email/password
├─ loginRateLimiter.reset(email)   ← Reset counter nếu đăng nhập thành công
├─ Tạo TokenSession (DB)           ← Lưu session refresh token
├─ generateRefreshToken(sessionId) ← Tạo refresh token
├─ generateAccessToken(userId)     ← Tạo access token
└─ Return { accessToken, refreshToken }
```

### 2. Sử Dụng API

```
GET /users/me
Authorization: Bearer <access_token>
│
├─ JwtAuthenticationFilter:
│   ├─ tokenBlacklistService.isRevoked(token) → false
│   ├─ jwtTokenProvider.validateToken(token)  → claims
│   └─ Set SecurityContext (username, role)
└─ Controller xử lý request
```

### 3. Refresh Token

```
POST /auth/refresh?refreshToken=<token>
│
├─ Tìm TokenSession theo refreshToken
├─ Kiểm tra session.isRevoked()
├─ Kiểm tra session.expiredAt < now()
├─ Tìm User từ session.userId
└─ generateAccessToken(userId) → { newAccessToken, refreshToken }
```

### 4. Logout

```
POST /auth/logout
Authorization: Bearer <access_token>
?refreshToken=<refresh_token>
│
├─ tokenBlacklistService.revoke(accessToken, 15 phút)  ← Redis TTL
├─ TokenSession.revoked = true  (DB)
└─ auditLogService.log(userId, LOGOUT)
```

---

## Rate Limiting Login

```java
// LoginRateLimiter.java
// Key: "login:<email>"
// Tối đa: 5 lần / 1 phút

public void check(String key) {
    Long attempts = redisTemplate.opsForValue().increment(key);
    if (attempts == 1) {
        redisTemplate.expire(key, Duration.ofMinutes(1));
    }
    if (attempts > 5) {
        throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
    }
}
```

---

## Token Blacklist

```java
// TokenBlacklistService.java
// Khi logout: lưu token vào Redis với TTL = thời gian còn lại của access token

public void revoke(String token, Duration ttl) {
    redisTemplate.opsForValue().set(token, "revoked", ttl);
}

public boolean isRevoked(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(token));
}
```

> **Lý do dùng Redis:** Access token là stateless (không thể invalidate trực tiếp). Redis lưu token bị thu hồi với TTL = thời gian hết hạn còn lại của token → tự động xóa khỏi Redis khi token hết hạn thật sự.

---

## Phân Quyền

### URL-level (SecurityConfig)

```java
// SecurityEndpoints.java — URL public (không cần token)
public static final String[] PUBLIC = {
    "/auth/**",
    "/swagger-ui/**",
    "/v3/api-docs/**"
};

// SecurityConfig — tất cả URL khác phải authenticated
.authorizeHttpRequests(auth -> auth
    .requestMatchers(PUBLIC).permitAll()
    .anyRequest().authenticated()
)
```

### Method-level (@PreAuthorize)

```java
// Chỉ ADMIN được gọi API này
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/search")
public ApiResponseSever<Page<UserResponse>> search(...) { ... }
```

### Role Prefix

Spring Security yêu cầu role phải có prefix `ROLE_`. Code xử lý nhất quán:

```java
// JwtAuthenticationFilter — khi parse token
new SimpleGrantedAuthority("ROLE_" + role)  // "ROLE_ADMIN"

// CustomUserDetails — khi load từ DB
new SimpleGrantedAuthority("ROLE_" + user.getRole().name())

// @PreAuthorize — Spring tự thêm "ROLE_" khi dùng hasRole()
@PreAuthorize("hasRole('ADMIN')")  // → Spring kiểm tra "ROLE_ADMIN"
```

---

## Public Endpoints

| Method | Path | Mô tả |
|--------|------|-------|
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/refresh` | Làm mới access token |
| POST | `/auth/logout` | Đăng xuất |
| GET | `/swagger-ui/**` | Swagger UI |
| GET | `/v3/api-docs/**` | OpenAPI spec |

---

## Secured Endpoints

| Method | Path | Role yêu cầu |
|--------|------|-------------|
| GET | `/users/me` | Bất kỳ (authenticated) |
| GET | `/users/search` | ADMIN |
| POST | `/users` | ADMIN |
| PUT | `/users/{id}` | ADMIN |
| DELETE | `/users/{id}` | ADMIN |
