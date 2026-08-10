# Phase 17 — Bước 6: Final release checklist

Ngày kiểm tra: 04/08/2026  
Trạng thái: **Release candidate đạt yêu cầu build/runtime; còn ESLint frontend là nợ kỹ thuật không chặn build.**

## 1. Phạm vi

Bước này không thêm nghiệp vụ mới. Việc thực hiện chỉ gồm kiểm tra cuối, bổ sung dữ liệu demo còn thiếu, loại runtime file khỏi Git và chuẩn hóa tài liệu release.

## 2. Kết quả build và test

| Hạng mục | Lệnh | Kết quả |
| --- | --- | --- |
| Backend compile, test, package | `gradlew.bat clean test bootJar --no-daemon` | PASS — `BUILD SUCCESSFUL` |
| Backend test | Gradle test report | PASS — 132 test, 0 failure, 0 error, 0 skipped |
| Backend artifact | `build/libs/base_v1-0.0.1-SNAPSHOT.jar` | PASS |
| Frontend production build | `npm.cmd run build` | PASS — 3.021 module được transform |
| Frontend lint | `npm.cmd run lint` | Chưa đạt — 111 error, 3 warning có sẵn trên nhiều màn hình |

Frontend lint chủ yếu là `no-explicit-any`, `react-refresh/only-export-components` và `react-hooks/set-state-in-effect`. CI hiện chỉ chạy type-check và production build, không chạy lint. Theo nguyên tắc không refactor lớn sát release, lỗi lint được ghi nhận làm backlog kỹ thuật thay vì sửa hàng loạt trong bước này.

Frontend build có cảnh báo bundle chính khoảng 1,78 MB trước gzip. Không ảnh hưởng chạy ứng dụng; nên code-split trong phase tối ưu tiếp theo.

## 3. OpenAPI/Swagger

- `OpenApiDocumentationTest` nằm trong bộ test đã pass.
- Swagger profile Docker mở thành công tại `/api/swagger-ui/index.html`, HTTP 200.
- Bearer JWT được khai báo ở OpenAPI components.
- Controller được nhóm theo tag module và có summary/response chính.
- Endpoint download trả file nhị phân trực tiếp; response JSON nghiệp vụ tiếp tục dùng `ApiResponseSever`.
- Profile production tắt API docs và Swagger UI.

## 4. Docker và runtime

Đã kiểm tra:

```text
docker compose --env-file .env.example -f docker-compose.yaml config --quiet     PASS
docker compose --env-file .env.prod.example -f docker-compose.prod.yaml config  PASS
docker compose up -d --build                                                     PASS
```

Trạng thái sau khi khởi động:

| Service | Kết quả |
| --- | --- |
| PostgreSQL 16 | Healthy |
| Redis 7 | Healthy |
| Backend | Healthy |
| Frontend Nginx | Healthy |

Runtime checks:

- `/api/actuator/health`: `UP`, gồm PostgreSQL, Redis và file storage `UP`.
- `/api/actuator/info`: trả đúng tên, mô tả và version ứng dụng.
- `/api/swagger-ui/index.html`: HTTP 200 ở profile Docker.
- Frontend `/healthz`: HTTP 200.
- Upload volume được mount tại `/app/uploads` và health indicator xác nhận read/write.

## 5. Flyway/database

Log Docker xác nhận:

```text
Successfully validated 25 migrations
Migrating schema public to version 23
Migrating schema public with repeatable migration demo data
Successfully applied 2 migrations, now at version v23
```

Database trắng được kiểm tra bằng `DatabaseReviewMigrationTest` trên PostgreSQL Testcontainers. Test xác nhận migration, foreign key, index, partial unique và các cột thời gian `timestamptz` quan trọng.

Không sửa migration version cũ. Schema mới chỉ được bổ sung bằng V23. Dữ liệu demo dùng repeatable migration riêng và không được nạp ở profile production.

## 6. Security

Các test unit/integration/security hiện tại đã pass. Cấu hình cuối:

- JWT secret production bắt buộc lấy từ environment.
- Access token mặc định 30 phút; refresh token mặc định 7 ngày.
- Access/refresh token có `jti`; refresh token chỉ lưu hash và có rotation.
- Logout revoke session hiện tại; logout-all revoke toàn bộ session của user.
- Filter đối chiếu `accessTokenJti` với session DB và lấy role hiện tại từ DB.
- User bị khóa hoặc đổi role bị revoke session.
- Swagger tắt trong production; Actuator production chỉ expose `health,info`, không expose toàn bộ endpoint.
- Attachment/download có auth, authorization, giới hạn dung lượng và bảo vệ path traversal.
- `.env`, secret, dump, backup, log, key và uploads đã được ignore.

Ba file upload runtime từng được Git theo dõi đã được bỏ khỏi index bằng `git rm --cached`; bản local vẫn được giữ nguyên.

## 7. Cache/performance

- Cache name tập trung trong `CacheNames`, không tạo tên cache rời rạc ở tài liệu/config.
- TTL có default và override theo nhóm dữ liệu.
- Test cache/invalidation thuộc bộ test đã pass.
- Kanban, task, time log, notification, project member, dashboard và analytics có chiến lược evict liên quan.
- Không cache file stream; dữ liệu batch import đang chạy không được coi là cache nghiệp vụ ổn định.
- Backend bật JDBC batch size và các repository quan trọng đã được rà soát N+1/projection ở Phase 14.

## 8. Demo data và smoke test

Demo data hiện có:

- 1 Admin.
- 1 Manager làm Owner và 1 Manager riêng làm Project Manager.
- Scrum Master, Product Owner, 2 Developer, QA/Tester, Viewer và user bị khóa.
- Project ở nhiều trạng thái.
- Sprint completed/active/planning/cancelled.
- Backlog, Task ở TODO/IN_PROGRESS/IN_REVIEW/BLOCKED/DONE/CANCELLED.
- Comment/reply, time log, import history/error, Review, Retrospective, dependency, Bug/QA, activity, notification và audit log.

Smoke test runtime đã pass:

```text
Login manager@hicas.demo                 code 1000
GET /api/users/me                       MANAGER
GET /api/projects?page=0&size=20        3 project có quyền xem
```

Trước buổi bảo vệ vẫn nên diễn tập thủ công trọn flow UI: tạo Project → thành viên → Backlog → Sprint → Task → Kanban realtime → Comment → Time Log → Dashboard/Notification/Activity → Excel/PDF → đóng Sprint.

## 9. README và cấu hình release

Đã chuẩn hóa:

- README root mô tả toàn bộ hệ thống, local/Docker, env, migration, test, demo account và flow demo.
- README backend bỏ nội dung template lỗi thời.
- README frontend bỏ nội dung Vite template.
- `Fe/.env.example` dùng backend Docker nội bộ làm default, không hard-code deployment cũ.
- `.env.example`, `.env.backend.example`, `.env.prod.example` được giữ làm template; `.env` thật bị ignore.

## 10. Kết luận

Các điều kiện chặn release đã pass: compile, test, package, frontend production build, Docker images, container health, Flyway, Swagger demo, JWT smoke test và dữ liệu demo.

Hai công việc không chặn release cần theo dõi:

1. Giảm 111 lỗi ESLint frontend theo từng module, tránh sửa hàng loạt trước demo.
2. Code-split bundle frontend lớn để cải thiện thời gian tải đầu tiên.

Project sẵn sàng chuyển sang Phase 18 — tài liệu đồ án và chuẩn bị demo bảo vệ.

