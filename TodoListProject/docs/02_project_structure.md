# 02 — Cấu Trúc Thư Mục

## Toàn Bộ Cây Thư Mục

```
Base_java_spring_boot/
├── docs/                          ← 📚 Tài liệu dự án (file này)
├── src/
│   ├── main/
│   │   ├── java/com/project/base_v1/
│   │   │   ├── BaseV1Application.java       ← Entry point
│   │   │   │
│   │   │   ├── config/                      ← Cấu hình Spring Beans
│   │   │   │   ├── AuditorAwareImpl.java    ← JPA Auditing: lấy user hiện tại
│   │   │   │   ├── CacheConfig.java         ← Cấu hình Redis CacheManager
│   │   │   │   ├── CacheNames.java          ← Hằng số tên cache
│   │   │   │   ├── CacheProperties.java     ← Bind config app.cache.* từ yaml
│   │   │   │   ├── JpaAuditConfig.java      ← @EnableJpaAuditing
│   │   │   │   ├── OpenApiConfig.java       ← Swagger/OpenAPI config
│   │   │   │   └── WebSocketConfig.java     ← STOMP WebSocket config
│   │   │   │
│   │   │   ├── controller/                  ← REST API endpoints
│   │   │   │   ├── AuthController.java      ← /auth/login, /refresh, /logout
│   │   │   │   └── UserController.java      ← /users/me, /search, CRUD
│   │   │   │
│   │   │   ├── dto/                         ← Data Transfer Objects (dùng Record)
│   │   │   │   ├── request/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   └── LoginRequest.java
│   │   │   │   │   └── user/
│   │   │   │   │       ├── CreateUserRequest.java
│   │   │   │   │       ├── UpdateUserRequest.java
│   │   │   │   │       └── UserSearchRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── auth/
│   │   │   │       │   └── AuthResponse.java
│   │   │   │       ├── core/
│   │   │   │       │   ├── ApiResponseSever.java   ← Wrapper chuẩn cho mọi response
│   │   │   │       │   └── ErrorResponseSever.java ← Cấu trúc lỗi
│   │   │   │       └── user/
│   │   │   │           └── UserResponse.java
│   │   │   │
│   │   │   ├── entity/                      ← JPA Entities
│   │   │   │   ├── BaseAuditEntity.java     ← Abstract class: createdAt/By, updatedAt/By, deletedAt/By
│   │   │   │   ├── AuditLog.java            ← Bảng audit_logs
│   │   │   │   ├── TokenSession.java        ← Bảng token_sessions (refresh token)
│   │   │   │   └── User.java                ← Bảng users
│   │   │   │
│   │   │   ├── enums/                       ← Enum constants
│   │   │   │   ├── AuditAction.java         ← LOGIN, LOGOUT, ...
│   │   │   │   └── UserRole.java            ← ADMIN, USER, ...
│   │   │   │
│   │   │   ├── exception/                   ← Xử lý lỗi tập trung
│   │   │   │   ├── BusinessException.java   ← Runtime exception tùy chỉnh
│   │   │   │   ├── ErrorCode.java           ← Enum tất cả mã lỗi
│   │   │   │   └── GlobalExceptionHandler.java ← @RestControllerAdvice
│   │   │   │
│   │   │   ├── mapper/                      ← MapStruct mappers
│   │   │   │   └── UserMapper.java          ← User entity ↔ UserResponse
│   │   │   │
│   │   │   ├── repository/                  ← Spring Data JPA repositories
│   │   │   │   ├── spec/
│   │   │   │   │   └── UserSpecification.java ← JPA Specification (dynamic query)
│   │   │   │   ├── AuditLogRepository.java
│   │   │   │   ├── TokenSessionRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   │
│   │   │   ├── security/                    ← Bảo mật
│   │   │   │   ├── CurrentUser.java         ← Util: lấy username từ SecurityContext
│   │   │   │   ├── CustomUserDetails.java   ← Wrapper User entity → UserDetails
│   │   │   │   ├── CustomUserDetailsService.java ← Load user từ DB theo email
│   │   │   │   ├── JwtAuthenticationFilter.java  ← Filter xác thực JWT mỗi request
│   │   │   │   ├── JwtTokenProvider.java    ← Tạo/validate JWT
│   │   │   │   ├── LoginRateLimiter.java    ← Giới hạn số lần đăng nhập sai
│   │   │   │   ├── SecurityBeans.java       ← Bean: PasswordEncoder
│   │   │   │   ├── SecurityConfig.java      ← SecurityFilterChain
│   │   │   │   ├── SecurityEndpoints.java   ← Danh sách URL public
│   │   │   │   └── TokenBlacklistService.java ← Blacklist access token vào Redis
│   │   │   │
│   │   │   └── service/                     ← Business logic
│   │   │       ├── AuditLogService.java      ← Interface
│   │   │       ├── AuditLogWebSocketService.java
│   │   │       ├── AuthService.java
│   │   │       ├── UserService.java
│   │   │       └── impl/                    ← Implementations
│   │   │           ├── AuditLogServiceImpl.java
│   │   │           ├── AuditLogWebSocketServiceImpl.java
│   │   │           ├── AuthServiceImpl.java
│   │   │           └── UserServiceImpl.java
│   │   │
│   │   └── resources/
│   │       ├── application.yaml             ← Cấu hình chính (dùng biến môi trường)
│   │       └── db/migration/
│   │           ├── V1__init_schema.sql      ← Tạo bảng
│   │           └── V2__data.sql             ← Seed data (admin account)
│   │
│   └── test/
│       └── resources/
│           └── application-test.yaml        ← Config cho test (H2 in-memory)
│
├── .env                                     ← Biến môi trường (KHÔNG commit)
├── .env.example                             ← Template env (commit)
├── build.gradle                             ← Dependencies
├── docker-compose.yaml                      ← PostgreSQL + Redis
└── docs/                                    ← Tài liệu
```

---

## Vai Trò Từng Package

### `config/`
Chứa tất cả `@Configuration` class. **Không chứa business logic**.
- Mỗi file config chỉ phụ trách một mối quan tâm (Single Responsibility).
- Các properties external được bind qua `@ConfigurationProperties`.

### `controller/`
- Chỉ nhận request, gọi service, trả response.
- **Không chứa business logic, không trực tiếp gọi repository**.
- Khai báo đầy đủ Swagger `@Operation`, `@ApiResponses`.

### `dto/`
- Tất cả đều là **Java Record** (immutable, không setter).
- Request DTO: có `@Valid` annotation.
- Response DTO: không expose thông tin nhạy cảm (password...).

### `entity/`
- Chỉ là JPA mapping, không chứa business logic.
- Extend `BaseAuditEntity` để tự động có audit fields.

### `enums/`
- Tập trung tất cả enum constants của domain.

### `exception/`
- `ErrorCode`: nguồn sự thật duy nhất cho tất cả mã lỗi.
- `GlobalExceptionHandler`: xử lý exception tập trung, không catch rải rác.

### `security/`
- Tách biệt hoàn toàn với business logic.
- `CurrentUser` là utility class để đọc thông tin user từ SecurityContext.

### `service/`
- Interface + Implementation tách biệt.
- Business logic chính nằm ở đây.
- Implementations ở package `impl/`.
