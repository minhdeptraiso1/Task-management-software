# 03 — Coding Conventions & Patterns

Tài liệu này mô tả các quy tắc code **bắt buộc** áp dụng nhất quán toàn dự án.

---

## 1. DTO — Dùng Java Record

> **Quy tắc:** Tất cả DTO (request & response) phải là `record`, không dùng class thông thường.

**Lý do:** Record tự động immutable, không có setter, giảm boilerplate, thể hiện rõ ý định "chỉ truyền dữ liệu".

### Request DTO

```java
// ✅ ĐÚNG — Dùng record + validation annotation
public record CreateUserRequest(

    @Schema(description = "Username", example = "admin")
    @NotBlank(message = "Username must not be blank")
    String username,

    @Schema(description = "Email", example = "admin@example.com")
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email must not be blank")
    String email,

    @Schema(description = "Password", example = "123456")
    @NotNull(message = "Password must not be null")
    String password,

    @Schema(description = "User role", example = "ADMIN")
    @NotNull(message = "Role must not be null")
    UserRole role
) {}
```

```java
// ❌ SAI — Không dùng class với Lombok cho request DTO
@Data
@Builder
public class CreateUserRequest {
    private String username;
    // ...
}
```

### Response DTO

```java
// ✅ ĐÚNG — Record đơn giản, không expose thông tin nhạy cảm
public record UserResponse(
    UUID id,
    String username,
    String email,
    Boolean enabled,
    UserRole role
) {}
```

### Optional fields trong Request

```java
// ✅ ĐÚNG — Field optional thì không @NotNull, type là wrapper (Boolean, không boolean)
public record UpdateUserRequest(
    String password,     // null = không đổi
    UserRole role,       // null = không đổi
    Boolean enabled      // null = không đổi
) {}
```

---

## 2. ErrorCode — Dùng Enum Tập Trung

> **Quy tắc:** Tất cả mã lỗi phải được định nghĩa trong enum `ErrorCode`, không hardcode string lỗi.

```java
public enum ErrorCode {

    // Format: TÊN(httpStatusCode_sequence, HttpStatus, "message")
    USER_NOT_FOUND(
        404001,
        HttpStatus.NOT_FOUND,
        "Không tìm thấy người dùng"
    ),

    EMAIL_ALREADY_EXISTS(
        409002,
        HttpStatus.CONFLICT,
        "Email đã tồn tại"
    ),

    TOO_MANY_REQUESTS(
        429001,
        HttpStatus.TOO_MANY_REQUESTS,
        "Bạn thao tác quá nhiều lần, vui lòng thử lại sau"
    );

    private final int code;
    private final HttpStatus status;
    private final String message;

    // Quy ước đánh số: [httpStatus][3 chữ số sequence]
    // VD: 400001, 400002, 401001, 404001, 409001...
}
```

### Cách throw lỗi

```java
// ✅ ĐÚNG — Dùng BusinessException + ErrorCode
throw new BusinessException(ErrorCode.USER_NOT_FOUND);

// ✅ ĐÚNG — Trong Optional chain
userRepository.findById(id)
    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

// ❌ SAI — Không throw RuntimeException trực tiếp
throw new RuntimeException("User not found");

// ❌ SAI — Không dùng ResponseStatusException rải rác
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
```

---

## 3. Lombok — Quy Tắc Dùng

> **Quy tắc:** Dùng `@FieldDefaults` + `@RequiredArgsConstructor` thay vì `@Autowired`.

### Class thông thường (Service, Component)

```java
// ✅ ĐÚNG
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;   // Tự inject qua constructor
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
}
```

```java
// ❌ SAI — Không dùng @Autowired field injection
@Service
public class UserServiceImpl {
    @Autowired
    private UserRepository userRepository;
}
```

### Entity

```java
// ✅ ĐÚNG — Entity dùng đầy đủ @Builder, @Getter, @Setter
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseAuditEntity {
    @Id
    UUID id;
    // ...
}
```

---

## 4. Response Wrapper — `ApiResponseSever<T>`

> **Quy tắc:** Tất cả response từ controller **phải** được bọc trong `ApiResponseSever<T>`.

```java
// Cấu trúc
public record ApiResponseSever<T>(
    boolean success,
    T data,
    ErrorResponseSever error
) {
    public static <T> ApiResponseSever<T> ok(T data) {
        return new ApiResponseSever<>(true, data, null);
    }
}
```

**Response thành công:**
```json
{
  "success": true,
  "data": { "id": "...", "username": "admin" },
  "error": null
}
```

**Response lỗi (từ GlobalExceptionHandler):**
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

### Cách dùng trong Controller

```java
// ✅ ĐÚNG
@GetMapping("/me")
public ApiResponseSever<UserResponse> me() {
    return ApiResponseSever.ok(userService.getCurrentUser(CurrentUser.username()));
}

@DeleteMapping("/{id}")
public ApiResponseSever<Void> delete(@PathVariable UUID id) {
    userService.deleteUserById(id);
    return ApiResponseSever.ok(null);   // Void operation trả null
}
```

---

## 5. Service — Interface + Implementation

> **Quy tắc:** Mọi service đều phải có interface và implementation tách biệt.

```
service/
├── UserService.java           ← Interface (public API)
└── impl/
    └── UserServiceImpl.java   ← Implementation
```

```java
// Interface — chỉ khai báo phương thức, không chứa logic
public interface UserService {
    UserResponse getCurrentUser(String username);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUserById(UUID userId);
    Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable);
}
```

---

## 6. Enum Cho Domain Constants

> **Quy tắc:** Role, status, action type... phải dùng enum, không dùng String.

```java
// ✅ ĐÚNG
public enum UserRole {
    ADMIN,
    USER
}

public enum AuditAction {
    LOGIN,
    LOGOUT
}
```

```java
// ❌ SAI — Không dùng String constant
public static final String ROLE_ADMIN = "ADMIN";
```

---

## 7. Configuration — Bind Properties Qua `@ConfigurationProperties`

> **Quy tắc:** Properties có nhiều field liên quan thì bind vào class riêng, không `@Value` từng field.

```java
// ✅ ĐÚNG — Nhóm các properties liên quan
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {
    private Duration defaultTtl = Duration.ofMinutes(10);
    private Map<String, Duration> ttl = new HashMap<>();
}

// Khai báo trong CacheConfig
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {
    private final CacheProperties cacheProperties;
    // ...
}
```

```java
// ❌ SAI — @Value rải rác trong nhiều class
@Value("${app.cache.default-ttl}")
private Duration defaultTtl;
```

---

## 8. Security — Đọc User Hiện Tại

> **Quy tắc:** Dùng `CurrentUser.username()` để lấy thông tin user đang đăng nhập.

```java
// ✅ ĐÚNG — Dùng utility class
String username = CurrentUser.username();
boolean isAuth = CurrentUser.isAuthenticated();
```

```java
// ❌ SAI — Không đọc SecurityContextHolder trực tiếp trong service
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String name = auth.getName();
```

> **Lưu ý quan trọng về Cache:** Khi dùng `@Cacheable`, **không** gọi `CurrentUser.username()` trong SpEL key. Phải truyền `username` làm tham số method và dùng `#username` làm key.

```java
// ✅ ĐÚNG
@Cacheable(value = CacheNames.USER_CURRENT, key = "#username")
public UserResponse getCurrentUser(String username) { ... }

// Controller
userService.getCurrentUser(CurrentUser.username());

// ❌ SAI — SpEL không có SecurityContextHolder context
@Cacheable(value = "...", key = "T(CurrentUser).username()")
public UserResponse getCurrentUser() { ... }
```

---

## 9. Soft Delete

> **Quy tắc:** Không xóa dữ liệu vật lý — dùng soft delete qua `deletedAt`.

```java
// ✅ ĐÚNG — Soft delete trong service
public void deleteUserById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.setDeletedAt(Instant.now());
    user.setDeletedBy(CurrentUser.username());
    userRepository.save(user);
}
```

Entity có `@SQLRestriction` để tự động lọc bản ghi đã xóa:

```java
@Entity
@SQLRestriction("deleted_at IS NULL")   // Mọi query tự động thêm điều kiện này
public class User extends BaseAuditEntity { ... }
```

---

## 10. Đặt Tên

| Loại | Convention | Ví dụ |
|------|-----------|-------|
| Class | PascalCase | `UserServiceImpl`, `AuthController` |
| Method | camelCase | `getCurrentUser`, `deleteUserById` |
| Field | camelCase | `userRepository`, `jwtTokenProvider` |
| Constant | UPPER_SNAKE_CASE | `MAX_ATTEMPTS`, `USER_DETAIL` |
| Package | lowercase | `com.project.base_v1.service` |
| DB table | snake_case | `users`, `token_sessions`, `audit_logs` |
| DB column | snake_case | `created_at`, `user_id`, `refresh_token` |
| Env var | UPPER_SNAKE_CASE | `DB_URL`, `JWT_SECRET`, `CACHE_TTL_USER_DETAIL` |
