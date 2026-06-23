# 05 — Error Handling

## Triết Lý

> **Một nơi duy nhất xử lý lỗi.** Tất cả exception được bắt ở `GlobalExceptionHandler`. Service chỉ cần `throw`, không cần `try-catch`.

---

## ErrorCode Enum

Tất cả mã lỗi định nghĩa trong `ErrorCode.java`. Mỗi entry có:
- `code` — mã số int (dùng cho client để map ra message)
- `status` — HTTP Status
- `message` — thông báo mặc định (tiếng Việt)

### Quy Tắc Đánh Số Code

```
Format: [HTTP_STATUS][3_chữ_số_sequence]

400001 → BAD_REQUEST (lỗi đầu tiên nhóm 400)
400002 → INVALID_REQUEST_BODY
401001 → INVALID_CREDENTIALS
401002 → TOKEN_EXPIRED
403001 → ACCESS_DENIED
404001 → USER_NOT_FOUND
409001 → USERNAME_ALREADY_EXISTS
429001 → TOO_MANY_REQUESTS
500001 → SYSTEM_ERROR
```

### Bảng Đầy Đủ ErrorCode

| Code | Tên | HTTP | Mô tả |
|------|-----|------|-------|
| 400001 | `BAD_REQUEST` | 400 | Yêu cầu không hợp lệ |
| 400002 | `INVALID_REQUEST_BODY` | 400 | Dữ liệu gửi lên không hợp lệ |
| 400003 | `VALIDATION_ERROR` | 400 | Dữ liệu không đúng định dạng |
| 400004 | `MISSING_REQUIRED_FIELD` | 400 | Thiếu thông tin bắt buộc |
| 400005 | `INVALID_PARAMETER` | 400 | Tham số không hợp lệ |
| 400006 | `INVALID_ENUM_VALUE` | 400 | Giá trị enum không hợp lệ |
| 400007 | `INVALID_DATE_FORMAT` | 400 | Định dạng ngày không hợp lệ |
| 400008 | `INVALID_UUID_FORMAT` | 400 | Định dạng UUID không hợp lệ |
| 401001 | `INVALID_CREDENTIALS` | 401 | Sai email/password |
| 401002 | `TOKEN_EXPIRED` | 401 | Token hết hạn |
| 401003 | `TOKEN_REVOKED` | 401 | Token bị thu hồi |
| 401004 | `INVALID_TOKEN` | 401 | Token không hợp lệ |
| 401005 | `UNAUTHENTICATED` | 401 | Chưa đăng nhập |
| 401006 | `REFRESH_TOKEN_EXPIRED` | 401 | Refresh token hết hạn |
| 401007 | `REFRESH_TOKEN_INVALID` | 401 | Refresh token không hợp lệ |
| 403001 | `ACCESS_DENIED` | 403 | Không có quyền |
| 403002 | `ACCOUNT_DISABLED` | 403 | Tài khoản bị khóa |
| 403003 | `ACCOUNT_NOT_ACTIVE` | 403 | Tài khoản chưa kích hoạt |
| 404001 | `USER_NOT_FOUND` | 404 | Không tìm thấy user |
| 404002 | `RESOURCE_NOT_FOUND` | 404 | Không tìm thấy resource |
| 404003 | `TOKEN_SESSION_NOT_FOUND` | 404 | Không tìm thấy session |
| 404004 | `API_NOT_FOUND` | 404 | API không tồn tại |
| 405001 | `METHOD_NOT_ALLOWED` | 405 | HTTP method không hỗ trợ |
| 409001 | `USERNAME_ALREADY_EXISTS` | 409 | Username đã tồn tại |
| 409002 | `EMAIL_ALREADY_EXISTS` | 409 | Email đã tồn tại |
| 409003 | `DATA_ALREADY_EXISTS` | 409 | Dữ liệu đã tồn tại |
| 409004 | `DATA_INTEGRITY_VIOLATION` | 409 | Vi phạm ràng buộc DB |
| 409005 | `FOREIGN_KEY_VIOLATION` | 409 | FK vi phạm |
| 415001 | `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-type không hỗ trợ |
| 429001 | `TOO_MANY_REQUESTS` | 429 | Quá nhiều lần thao tác |
| 429002 | `LOGIN_TOO_MANY_ATTEMPTS` | 429 | Đăng nhập sai quá nhiều |
| 500001 | `SYSTEM_ERROR` | 500 | Lỗi hệ thống |
| 500002 | `DATABASE_ERROR` | 500 | Lỗi database |
| 500003 | `TRANSACTION_ERROR` | 500 | Lỗi transaction |
| 500004 | `LAZY_LOADING_ERROR` | 500 | Lỗi lazy loading JPA |
| 500005 | `REDIS_ERROR` | 500 | Lỗi Redis |
| 500006 | `CACHE_ERROR` | 500 | Lỗi cache |
| 500007 | `JSON_PROCESSING_ERROR` | 500 | Lỗi JSON |
| 500008 | `WEBSOCKET_ERROR` | 500 | Lỗi WebSocket |

---

## BusinessException

```java
// BusinessException.java — runtime exception của domain
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }
}
```

### Cách throw trong service

```java
// Pattern 1: Trực tiếp
throw new BusinessException(ErrorCode.ACCESS_DENIED);

// Pattern 2: Trong Optional chain (phổ biến nhất)
User user = userRepository.findById(id)
    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

// Pattern 3: Validate điều kiện
if (userRepository.existsByEmail(request.email())) {
    throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
}
```

---

## GlobalExceptionHandler

`GlobalExceptionHandler` (@RestControllerAdvice) bắt tất cả exception và trả về cấu trúc chuẩn:

### Danh Sách Exception Được Bắt

| Exception | Nguồn | Xử lý |
|-----------|-------|-------|
| `BusinessException` | Domain logic | Dùng ErrorCode có sẵn |
| `MethodArgumentNotValidException` | `@Valid` trên @RequestBody | Lấy field + message đầu tiên |
| `ConstraintViolationException` | `@Valid` trên @RequestParam | Lấy violation đầu tiên |
| `MissingServletRequestParameterException` | Thiếu required param | Thông báo tên param thiếu |
| `MethodArgumentTypeMismatchException` | Sai kiểu UUID, int... | Thông báo kiểu cần thiết |
| `HttpMessageNotReadableException` | JSON sai format/enum sai | Thông báo field sai |
| `DataIntegrityViolationException` | Unique/FK violation DB | Phát hiện loại violation |
| `CannotCreateTransactionException` | DB offline | DATABASE_ERROR |
| `TransactionSystemException` | Lỗi commit/rollback | TRANSACTION_ERROR |
| `LazyInitializationException` | JPA lazy load | LAZY_LOADING_ERROR |
| `DataAccessException` | Spring Data lỗi chung | DATABASE_ERROR |
| `Exception` | Fallback cuối cùng | SYSTEM_ERROR |

### Response Lỗi Chuẩn

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

---

## Ví Dụ Thực Tế

### Validation Error (400)

**Request:**
```json
POST /users
{ "username": "", "email": "invalid", "password": "123" }
```

**Response:**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": 400003,
    "message": "username: Username must not be blank"
  }
}
```

### Business Error (409)

**Request:**
```json
POST /users
{ "username": "admin", "email": "new@email.com", "password": "123456", "role": "USER" }
```

**Response (username đã tồn tại):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": 409001,
    "message": "Tên đăng nhập đã tồn tại"
  }
}
```

### Rate Limit (429)

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": 429001,
    "message": "Bạn thao tác quá nhiều lần, vui lòng thử lại sau"
  }
}
```

---

## Thêm ErrorCode Mới

1. Mở `ErrorCode.java`
2. Thêm enum entry với code số tiếp theo trong nhóm HTTP status tương ứng
3. Dùng `throw new BusinessException(ErrorCode.TEN_MOI)` ở nơi cần

```java
// Ví dụ thêm lỗi mới
PRODUCT_OUT_OF_STOCK(
    400009,
    HttpStatus.BAD_REQUEST,
    "Sản phẩm đã hết hàng"
),
```
