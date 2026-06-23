# 06 — Redis Cache

## Tổng Quan

Redis được dùng cho 3 mục đích:

| Mục đích | Key pattern | TTL |
|---------|------------|-----|
| **Cache data** | `user_detail::uuid`, `user_current::username`... | Config qua env |
| **Token Blacklist** | Raw token string | = thời gian còn lại của access token |
| **Rate Limiting** | `login:<email>` | 1 phút |

---

## Cấu Hình Cache

### `application.yaml`

```yaml
spring:
  cache:
    type: redis

app:
  cache:
    default-ttl: ${CACHE_DEFAULT_TTL}   # fallback nếu cache không có TTL riêng
    ttl:
      user_detail: ${CACHE_TTL_USER_DETAIL}
      user_current: ${CACHE_TTL_USER_CURRENT}
      user_search: ${CACHE_TTL_USER_SEARCH}
```

### `.env`

```env
CACHE_DEFAULT_TTL=10m
CACHE_TTL_USER_DETAIL=10m
CACHE_TTL_USER_CURRENT=5m
CACHE_TTL_USER_SEARCH=2m
```

### `CacheConfig.java`

- JSON serializer với type info (`@class` field) — đảm bảo deserialize đúng class
- Mỗi cache có TTL riêng lấy từ `CacheProperties`
- `transactionAware()` — cache chỉ commit khi transaction thành công

---

## Tên Cache — `CacheNames.java`

```java
public final class CacheNames {
    public static final String USER_DETAIL  = "user_detail";
    public static final String USER_CURRENT = "user_current";
    public static final String USER_SEARCH  = "user_search";
}
```

> **Quy tắc:** Luôn dùng constant từ `CacheNames`, không hardcode string tên cache.

---

## Sử Dụng Cache Annotations

### `@Cacheable` — Đọc từ cache, nếu miss thì gọi method

```java
// Cache kết quả tìm kiếm theo userId
@Cacheable(value = CacheNames.USER_DETAIL, key = "#userId")
public UserResponse getUserById(UUID userId) { ... }

// Cache user hiện tại theo username
// ⚠️ Phải truyền username qua tham số, không đọc từ SecurityContext trong SpEL
@Cacheable(value = CacheNames.USER_CURRENT, key = "#username")
public UserResponse getCurrentUser(String username) { ... }

// Cache danh sách search với composite key
@Cacheable(
    value = CacheNames.USER_SEARCH,
    key = "'keyword=' + (#request.keyword() == null ? '' : #request.keyword())"
        + " + '|role=' + (#request.role() == null ? '' : #request.role())"
        + " + '|page=' + #pageable.pageNumber"
        + " + '|size=' + #pageable.pageSize"
)
public Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable) { ... }
```

### `@CachePut` — Luôn gọi method VÀ cập nhật cache

```java
// Sau khi update → ghi kết quả mới vào cache USER_DETAIL
@CachePut(value = CacheNames.USER_DETAIL, key = "#id")
public UserResponse updateUser(UUID id, UpdateUserRequest request) { ... }
```

### `@CacheEvict` — Xóa cache

```java
// Xóa entry cụ thể
@CacheEvict(value = CacheNames.USER_DETAIL, key = "#userId")
public void deleteUserById(UUID userId) { ... }

// Xóa toàn bộ entries trong một cache (khi data thay đổi)
@CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true)
```

### `@Caching` — Kết hợp nhiều annotation

```java
@Caching(
    put = {
        @CachePut(value = CacheNames.USER_DETAIL, key = "#id")
    },
    evict = {
        @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true),
        @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true)
    }
)
public UserResponse updateUser(UUID id, UpdateUserRequest request) { ... }
```

---

## Chiến Lược Invalidation

| Sự kiện | Cache bị xóa |
|--------|-------------|
| Tạo user mới | `user_search` (all), `user_current` (all) |
| Cập nhật user | `user_detail` (entry), `user_current` (all), `user_search` (all) |
| Xóa user | `user_detail` (entry), `user_current` (all), `user_search` (all) |

---

## Token Blacklist (Redis)

```java
// Khi logout — lưu access token với TTL 15 phút
tokenBlacklistService.revoke(accessToken, Duration.ofMinutes(15));

// Mỗi request — kiểm tra token có trong blacklist không
boolean revoked = tokenBlacklistService.isRevoked(token);
```

**Key trong Redis:** Raw access token (JWT string)
**Value:** `"revoked"`
**TTL:** = thời gian hết hạn còn lại của token (token sẽ tự hết hạn, Redis entry cũng sẽ xóa theo)

---

## Rate Limiting (Redis)

```java
// Key: "login:<email>"
// Logic: increment counter, set TTL 1 phút lần đầu, throw nếu > 5
LoginRateLimiter.check("login:" + email);
LoginRateLimiter.reset("login:" + email);  // Reset sau login thành công
```

---

## Thêm Cache Mới

**Bước 1:** Thêm constant vào `CacheNames.java`

```java
public static final String MY_ENTITY = "my_entity";
```

**Bước 2:** Thêm TTL vào `application.yaml`

```yaml
app:
  cache:
    ttl:
      my_entity: ${CACHE_TTL_MY_ENTITY}
```

**Bước 3:** Thêm biến vào `.env` và `.env.example`

```env
CACHE_TTL_MY_ENTITY=5m
```

**Bước 4:** Dùng annotation trong service

```java
@Cacheable(value = CacheNames.MY_ENTITY, key = "#id")
public MyEntityResponse getById(UUID id) { ... }
```
