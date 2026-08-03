# Báo cáo tổng hợp kiểm thử Phase 15

## 1. Tổng quan

| Thuộc tính | Kết quả |
|---|---|
| Ngày xác nhận | 03/08/2026 |
| Công cụ | JUnit 5, Mockito, AssertJ, Spring Boot Test, MockMvc, Testcontainers |
| Java / Spring Boot | Java 21 / Spring Boot 3.5.1 |
| Database kiểm thử | PostgreSQL 16 Alpine qua Testcontainers |
| Redis kiểm thử | Redis 7 Alpine qua Testcontainers |
| Tổng test | 113 |
| Passed | 113 |
| Failed / Error / Skipped | 0 / 0 / 0 |
| Kết luận | PASS |

Test không sử dụng database dev, không gọi API ngoài và không phụ thuộc dữ liệu cũ. PostgreSQL và Redis được tạo độc lập bằng Testcontainers. File phát sinh trong test được giới hạn tại `build/test-uploads`.

## 2. Unit và configuration test

| Nhóm | Nội dung chính | Trạng thái |
|---|---|---|
| Validator | Sprint, Task, chuyển trạng thái Kanban, time log, file security | PASS |
| Service | Workflow Sprint, Task, import, analytics, concurrency và position | PASS |
| Access | Quyền Project, Task và chính sách ADMIN chỉ xem | PASS |
| Notification / Activity | Recipient, target URL và nội dung activity | PASS |
| Cache / CORS | Cache key, cache eviction, cache config và CORS properties | PASS |

## 3. Repository test

| Repository | Nội dung chính | Trạng thái |
|---|---|---|
| ProjectRepository | Soft delete và truy vấn Project | PASS |
| SprintRepository | Ràng buộc và truy vấn Sprint | PASS |
| TaskRepository | Position, specification và soft-delete filter | PASS |
| TaskTimeLogRepository | Tổng hợp số phút làm việc | PASS |
| NotificationRecipientRepository | Danh sách và unread count | PASS |
| ProjectActivityRepository | Truy vấn activity theo dự án | PASS |

Repository test chạy với PostgreSQL thật trong container, không thay thế bằng H2.

## 4. Integration test

| Luồng | Nội dung chính | Trạng thái |
|---|---|---|
| Auth | Login, token và current user | PASS |
| Project | Tạo, xem và phân quyền dự án | PASS |
| Agile workflow | Project → Sprint → Backlog → Task | PASS |
| API security | JWT, role, ownership và endpoint public/private | PASS |

## 5. Atomic transaction test

| Luồng | Nội dung chính | Trạng thái |
|---|---|---|
| Task import | Dòng không hợp lệ rollback toàn bộ batch | PASS |
| Transaction propagation | Runtime/checked exception và rollback boundary | PASS |
| Sprint/Task synchronization | Không để lại trạng thái một phần khi lỗi | PASS |

## 6. Security test

| Trường hợp | Kết quả mong đợi | Trạng thái |
|---|---|---|
| JWT thiếu, sai hoặc hết hạn | Trả `401` | PASS |
| User bị khóa hoặc đổi role | Token cũ mất hiệu lực | PASS |
| Project access | Người ngoài dự án bị chặn | PASS |
| Task assignment | Thành viên dự án được gán; outsider và ADMIN bị chặn | PASS |
| ADMIN view-only | Không được thao tác nghiệp vụ Project/Task | PASS |
| Comment ownership | Không sửa comment của người khác | PASS |
| Time log ownership | Không sửa time log của người khác | PASS |
| Notification ownership | Dữ liệu người khác trả `404` | PASS |
| Attachment security | Chặn path traversal và file nguy hiểm | PASS |
| Login rate limit | Request vượt ngưỡng bị chặn | PASS |

## 7. Lệnh chạy

```powershell
# Toàn bộ test
.\gradlew.bat clean test

# Một class
.\gradlew.bat test --tests "com.project.taskmanagement.service.validation.TaskStatusTransitionValidatorTest"

# Nhóm integration
.\gradlew.bat test --tests "com.project.taskmanagement.integration.*"

# Nhóm security
.\gradlew.bat test --tests "com.project.taskmanagement.security.*"
```

HTML report được tạo tại `build/reports/tests/test/index.html` sau khi chạy test.

## 8. Nhận xét

Hệ thống đã được kiểm thử ở nhiều mức gồm unit, repository, integration, atomic transaction và security. Bộ test tập trung vào workflow Sprint/Task, query PostgreSQL, tính nguyên tử của transaction, JWT/session, quyền truy cập và ownership dữ liệu. Kết quả hiện tại đủ làm mốc hồi quy cho các Phase triển khai tiếp theo.
