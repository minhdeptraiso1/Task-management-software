# Phase 17 bước 5 — Database Review

## Phạm vi rà soát

- 22 migration hiện hữu từ `V1` đến `V22`.
- 26 entity và toàn bộ repository query.
- Foreign key, unique/check constraint, partial index, soft delete, enum mapping, UUID và timezone.
- Dữ liệu demo trong `R__demo_data.sql`.

Không migration cũ nào bị chỉnh sửa. Mọi thay đổi database nằm trong `V23__database_review_indexes_constraints.sql`.

## Kết quả

### Nội dung đã đúng và được giữ nguyên

- Bảng và column dùng snake_case; Java entity dùng camelCase.
- Entity nghiệp vụ chính dùng UUIDv7 từ `BaseIdEntity`; service không tự sinh UUID ngẫu nhiên.
- Project, ProjectMember, Sprint, BacklogItem, Task, Comment, Time Log, Attachment, Bug và các bảng cộng tác dùng soft delete cùng `@SQLRestriction`.
- Task luôn có `backlog_item_id`; Sprint hiện tại và Sprint gốc là nullable, không bị mất khi hủy Sprint.
- Một Project chỉ có tối đa một Sprint `ACTIVE` qua partial unique index từ V7.
- JSON audit/activity dùng `TEXT`, không bị giới hạn 255 ký tự.
- Các ngày nghiệp vụ tiếp tục dùng `DATE`.
- Các enum entity dùng `EnumType.STRING` và column có độ dài từ 30 đến 100.

### Khoảng trống được sửa trong V23

- Bổ sung FK cho token session, audit log, task dependency, người chặn Task, Sprint Review và Retrospective.
- Bỏ `ON DELETE CASCADE` khỏi notification recipient nghiệp vụ.
- Chuyển unique username, email, Project code, notification recipient, Sprint Review và Retrospective sang partial unique index phù hợp soft delete.
- Username, email và Project code được unique không phân biệt hoa thường.
- Thêm check constraint theo enum cho User, Project, ProjectMember, Sprint, Backlog Item, Task, Task Import và Bug.
- Giới hạn mỗi Time Log tối đa 720 phút, không cho task tự phụ thuộc chính nó, không cho file có dung lượng bằng 0 và không cho `reopened_count` âm.
- Bổ sung composite partial index cho Project list, membership, Sprint, Backlog, Kanban/Task dashboard, comment, Time Log, notification, activity, token session, import audit và Bug report.
- Chuyển các column cũ đang map với Java `Instant` từ `TIMESTAMP` sang `TIMESTAMP WITH TIME ZONE`.

## An toàn dữ liệu khi chạy V23

Trước khi tạo case-insensitive unique index, migration tự kiểm tra:

- username active trùng theo `LOWER(username)`;
- email active trùng theo `LOWER(email)`;
- Project code active trùng theo `LOWER(code)`.

Nếu có dữ liệu trùng, migration dừng với thông báo rõ ràng thay vì tự xóa hoặc tự gộp dữ liệu. Cần xử lý dữ liệu theo quyết định nghiệp vụ rồi chạy lại migration.

Timezone legacy được hiểu là UTC khi chuyển sang `TIMESTAMP WITH TIME ZONE`, khớp cấu hình `hibernate.jdbc.time_zone=UTC`. Trước khi triển khai lên database cũ nên tạo backup và xác nhận timezone của PostgreSQL/JDBC đang là UTC.

## Query kiểm tra trước triển khai

```sql
SELECT LOWER(username), COUNT(*)
FROM users
WHERE deleted_at IS NULL
GROUP BY LOWER(username)
HAVING COUNT(*) > 1;

SELECT LOWER(email), COUNT(*)
FROM users
WHERE deleted_at IS NULL
GROUP BY LOWER(email)
HAVING COUNT(*) > 1;

SELECT LOWER(code), COUNT(*)
FROM projects
WHERE deleted_at IS NULL
GROUP BY LOWER(code)
HAVING COUNT(*) > 1;

SELECT project_id, user_id, COUNT(*)
FROM project_members
WHERE deleted_at IS NULL
GROUP BY project_id, user_id
HAVING COUNT(*) > 1;

SELECT project_id, COUNT(*)
FROM sprints
WHERE status = 'ACTIVE' AND deleted_at IS NULL
GROUP BY project_id
HAVING COUNT(*) > 1;

SELECT id, minutes
FROM task_time_logs
WHERE deleted_at IS NULL AND (minutes <= 0 OR minutes > 720);
```

## Mapping entity được cập nhật

`unique = true` được bỏ khỏi `User.username`, `User.email` và `Project.code`. Unique của ba field này hiện do PostgreSQL partial expression index quản lý; annotation JPA không biểu diễn được `LOWER(...) WHERE deleted_at IS NULL`.

## Kiểm thử

`DatabaseReviewMigrationTest` chạy với PostgreSQL 16 Testcontainers và xác nhận:

- V23 chạy sạch trên database mới;
- các FK và index quan trọng tồn tại;
- các field Java `Instant` dùng `TIMESTAMP WITH TIME ZONE`;
- khác biệt hoa/thường bị chặn với dữ liệu active;
- giá trị đã soft delete không chiếm unique key.

Toàn bộ repository/integration test tiếp tục chạy trên PostgreSQL thật, không dùng H2.
