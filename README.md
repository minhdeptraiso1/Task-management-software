# HICAS Task Management

Hệ thống quản lý dự án và công việc nội bộ theo Agile/Scrum, gồm giao diện React và REST API Spring Boot. Hệ thống hỗ trợ quản lý Project, thành viên, Product Backlog, Sprint, Kanban Task, Bug/QA, bình luận, time log, thông báo realtime, dashboard và báo cáo Excel/PDF.

## Công nghệ

- Frontend: React 19, TypeScript, Vite, Tailwind CSS.
- Backend: Java 21, Spring Boot 3.5, Spring Security, JPA, WebSocket/STOMP.
- Dữ liệu: PostgreSQL 16, Flyway, Redis.
- Báo cáo: Apache POI và OpenPDF.
- Vận hành: Docker Compose, Nginx, Caddy, GitHub Actions, Actuator.
- Kiểm thử: JUnit 5, Spring Boot Test, Spring Security Test và Testcontainers.

## Cấu trúc

```text
PhamVanThiet/
├── Fe/                    React frontend
├── TodoListProject/       Spring Boot backend, Docker Compose và tài liệu vận hành
└── .github/workflows/     CI build/test frontend, backend và Docker image
```

## Yêu cầu môi trường

Chạy toàn bộ bằng Docker chỉ cần Docker Desktop hoặc Docker Engine có Compose V2. Chạy thủ công cần thêm JDK 21, Node.js 22 và PostgreSQL/Redis.

## Chạy nhanh bằng Docker

Tại thư mục `TodoListProject`:

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

Trước khi chạy, đổi ít nhất `DB_PASSWORD` và `JWT_SECRET` trong `.env`.

- Giao diện: `http://localhost:5173`
- API: `http://localhost:8080/api`
- Swagger demo: `http://localhost:8080/api/swagger-ui/index.html`
- Health: `http://localhost:8080/api/actuator/health`

Frontend dùng Nginx proxy `/api` sang backend nên trình duyệt không cần cấu hình địa chỉ backend riêng khi chạy full stack.

## Chạy local

Khởi động PostgreSQL và Redis, sau đó tạo `TodoListProject/.env` từ `.env.example`.

Backend:

```powershell
cd TodoListProject
.\gradlew.bat bootRun
```

Frontend:

```powershell
cd Fe
npm ci
npm run dev
```

Profile `dev` và `docker` nạp dữ liệu demo. Profile `prod` không nạp dữ liệu demo và tắt Swagger.

## Biến môi trường quan trọng

| Biến | Ý nghĩa |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Kết nối PostgreSQL |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Kết nối Redis |
| `JWT_SECRET` | Khóa ký JWT, tối thiểu 32 ký tự |
| `JWT_ACCESS_EXP_MINUTES` | Thời hạn access token, mặc định 30 phút |
| `JWT_REFRESH_EXP_DAYS` | Thời hạn refresh token, mặc định 7 ngày |
| `FILE_STORAGE_ROOT_PATH` | Thư mục lưu attachment |
| `CORS_ALLOWED_ORIGINS` | Danh sách origin frontend được phép |
| `VITE_API_URL` | API build-time của frontend, Docker nên giữ `/api` |
| `NGINX_BACKEND_URL`, `NGINX_BACKEND_HOST` | Backend runtime mà Nginx proxy tới |

Mẫu đầy đủ:

- Full stack: `TodoListProject/.env.example`
- Backend độc lập: `TodoListProject/.env.backend.example`
- Production: `TodoListProject/.env.prod.example`
- Frontend độc lập: `Fe/.env.example`

Không commit `.env`, secret thật, database dump, log, private key hoặc file upload runtime.

## Database và migration

Flyway tự chạy khi backend khởi động. Migration schema nằm tại `TodoListProject/src/main/resources/db/migration` và không được sửa sau khi đã phát hành. Thay đổi schema tiếp theo phải tạo version migration mới.

Dữ liệu trình diễn nằm tại `TodoListProject/src/main/resources/db/demo/R__demo_data.sql`, chỉ được nạp trong profile `dev` và `docker`.

## Tài khoản demo

Mật khẩu chung: `123456`.

| Vai trò | Email đăng nhập |
| --- | --- |
| Admin hệ thống | `admin@example.com` |
| Manager/Owner | `manager@hicas.demo` |
| Project Manager | `pm@hicas.demo` |
| Scrum Master | `scrum@hicas.demo` |
| Product Owner | `po@hicas.demo` |
| Developer | `dev.minh@hicas.demo` |
| QA/Tester | `qa.linh@hicas.demo` |

`locked.demo` là tài khoản bị khóa dùng để kiểm tra security. Không sử dụng các mật khẩu demo trong production.

## Build và test

Backend:

```powershell
cd TodoListProject
.\gradlew.bat clean test bootJar --no-daemon
```

Frontend:

```powershell
cd Fe
npm ci
npm run build
```

Integration/repository test dùng PostgreSQL Testcontainers, không sử dụng database dev của máy lập trình.

## Module chính

- Authentication, refresh rotation, logout và logout-all.
- User access, Admin dashboard và system audit.
- Project, Project Member và Activity Feed.
- Product Backlog, Sprint workflow, capacity, health, review và retrospective.
- Task, Kanban realtime, dependency, blocker, risk, comment và time log.
- Bug/QA, evidence và attachment.
- Notification realtime, reminder và daily digest.
- Global search, advanced filter, dashboard và analytics.
- Xuất báo cáo Excel/PDF theo Project, Sprint và khoảng thời gian.

## Kịch bản demo đề xuất

1. Đăng nhập Manager và chọn Project `HICAS-DEMO`.
2. Kiểm tra thành viên và các vai trò Project.
3. Tạo Backlog Item và đưa vào Sprint đang lập kế hoạch.
4. Bắt đầu Sprint, tạo và phân công Task.
5. Kéo Task qua các cột Kanban và quan sát realtime ở trình duyệt thứ hai.
6. Bình luận, ghi time log, tạo Bug và đính kèm file.
7. Xem Notification, Activity, Dashboard và báo cáo Sprint.
8. Xuất Excel/PDF, hoàn tất Sprint và kiểm tra Task/Comment/TimeLog vẫn được bảo toàn.

## Security và giới hạn vận hành

- Access token được đối chiếu với token session; refresh token có rotation và chỉ lưu hash.
- `logout-all`, khóa tài khoản hoặc đổi role sẽ revoke toàn bộ session liên quan.
- Production tắt Swagger, không nạp demo data và chỉ expose Actuator `health/info`.
- Attachment có kiểm tra quyền, loại file, kích thước và chống path traversal.
- File upload lưu trên local volume; khi chạy nhiều backend instance cần chuyển sang object storage dùng chung.
- Frontend production hiện có cảnh báo bundle lớn; build vẫn thành công nhưng nên code-split ở giai đoạn tối ưu tiếp theo.

## Tài liệu thêm

- [Hướng dẫn triển khai](TodoListProject/README_DEPLOY.md)
- [Tài liệu backend](TodoListProject/README.md)
- [Database review](TodoListProject/docs/13_phase_17_database_review.md)
- [OpenAPI review](TodoListProject/docs/12_phase_17_openapi_swagger.md)
- [Final release checklist](TodoListProject/docs/14_phase_17_final_release_checklist.md)
