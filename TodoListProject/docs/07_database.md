# 07 — Database & Entities

## Database Schema

### Bảng `users`

```sql
CREATE TABLE users (
    id         UUID         PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,          -- ADMIN | USER
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit fields (tự điền bởi JPA Auditing)
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Soft delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);
```

### Bảng `token_sessions`

```sql
CREATE TABLE token_sessions (
    id            UUID      PRIMARY KEY,
    user_id       UUID      NOT NULL,
    refresh_token TEXT      NOT NULL,
    revoked       BOOLEAN   NOT NULL DEFAULT FALSE,
    expired_at    TIMESTAMP NOT NULL
);
```

> **Lưu ý:** Nên thêm index: `CREATE INDEX idx_token_sessions_refresh_token ON token_sessions(refresh_token);`

### Bảng `audit_logs`

```sql
CREATE TABLE audit_logs (
    id         UUID         PRIMARY KEY,
    user_id    UUID,                           -- nullable (system actions)
    action     VARCHAR(255) NOT NULL,          -- LOGIN | LOGOUT | ...
    created_at TIMESTAMP    NOT NULL
);
```

---

## Flyway Migration

- Migration files đặt ở: `src/main/resources/db/migration/`
- Tên file: `V{number}__{description}.sql`
- Flyway tự chạy khi ứng dụng khởi động

```
V1__init_schema.sql   → Tạo toàn bộ bảng
V2__data.sql          → Seed data (admin account)
V3__...sql            → Migration tiếp theo (nếu có)
```

> **Quy tắc:** Không bao giờ sửa file migration đã commit. Chỉ thêm file mới với số version tiếp theo.

---

## BaseAuditEntity

Tất cả entity cần audit phải extend `BaseAuditEntity`:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @CreatedDate
    Instant createdAt;      // Tự điền khi INSERT

    @LastModifiedDate
    Instant updatedAt;      // Tự cập nhật khi UPDATE

    @CreatedBy
    String createdBy;       // Tự điền username người tạo

    @LastModifiedBy
    String updatedBy;       // Tự cập nhật username người sửa

    Instant deletedAt;      // Soft delete timestamp
    String deletedBy;       // Soft delete by
}
```

Username được lấy từ `AuditorAwareImpl`:

```java
@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CurrentUser.username());
    }
}
```

---

## Soft Delete

Entity có soft delete dùng `@SQLRestriction` (Hibernate 6+):

```java
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")   // Tự động filter trong mọi query
public class User extends BaseAuditEntity { ... }
```

### Cách soft delete trong service

```java
public void deleteUserById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.setDeletedAt(Instant.now());
    user.setDeletedBy(CurrentUser.username());
    userRepository.save(user);
    // ⚠️ Không dùng userRepository.delete(user) — đó là hard delete
}
```

> **Lưu ý:** `@SQLRestriction("deleted_at IS NULL")` đã thay thế `@Where` (deprecated Hibernate 6).
> Nếu code vẫn dùng `@Where`, cần migrate sang `@SQLRestriction`.

---

## Entity Pattern

```java
@Entity
@Table(name = "ten_bang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")    // Nếu có soft delete
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyEntity extends BaseAuditEntity {

    @Id
    UUID id;                              // UUID, không dùng Long auto-increment

    @Column(nullable = false, unique = true)
    String name;

    @Enumerated(EnumType.STRING)          // Lưu tên enum, không lưu ordinal
    @Column(nullable = false)
    MyEnum status;
}
```

---

## JPA Specification — Dynamic Query

Dùng cho tìm kiếm linh hoạt (nhiều điều kiện optional):

```java
// UserSpecification.java
public class UserSpecification {

    // Trả null nếu giá trị filter là null → Spring Data tự bỏ qua điều kiện đó
    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("username")), like),
                cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) ->
            role == null ? null : cb.equal(root.get("role"), role);
    }
}

// Dùng trong service
Specification<User> spec = Specification.allOf(
    UserSpecification.hasKeyword(request.keyword()),
    UserSpecification.hasRole(request.role()),
    UserSpecification.isEnabled(request.enabled())
);
Page<User> users = userRepository.findAll(spec, pageable);
```

---

## Repository Pattern

```java
// Extends JpaRepository + JpaSpecificationExecutor nếu cần Specification
public interface UserRepository
    extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Spring Data tự sinh query từ tên method
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

---

## Audit Log

Mỗi khi có action quan trọng → ghi vào `audit_logs` và push realtime qua WebSocket:

```java
// Ghi audit log
auditLogService.log(userId, AuditAction.LOGIN.name());
auditLogService.log(userId, AuditAction.LOGOUT.name());

// AuditLogService tự:
// 1. Save vào DB (audit_logs)
// 2. Push realtime qua WebSocket topic /topic/audit-logs
```

**WebSocket endpoint:** `ws://<host>/api/ws`
**Subscribe topic:** `/topic/audit-logs`
