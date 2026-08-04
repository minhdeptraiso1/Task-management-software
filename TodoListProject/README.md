# HICAS Task Management Backend

REST API Spring Boot cho hệ thống quản lý dự án và công việc Agile/Scrum. README tổng thể, tài khoản demo và kịch bản trình diễn nằm tại [README của dự án](../README.md).

## Yêu cầu

- JDK 21.
- Docker Compose V2 nếu chạy PostgreSQL, Redis hoặc toàn bộ hệ thống bằng container.

## Chạy bằng Docker

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

Địa chỉ mặc định:

- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui/index.html`
- Health: `http://localhost:8080/api/actuator/health`
- Info: `http://localhost:8080/api/actuator/info`

## Chạy local

Tạo `.env` từ `.env.example`, cấu hình PostgreSQL/Redis local rồi chạy:

```powershell
.\gradlew.bat bootRun
```

Profile mặc định là `dev`. Profile này bật Swagger và nạp dữ liệu trình diễn. Production phải dùng `SPRING_PROFILES_ACTIVE=prod`.

## Build và test

```powershell
.\gradlew.bat clean test bootJar --no-daemon
```

Các test repository/integration dùng PostgreSQL Testcontainers và Flyway migration từ database trắng.

## Cấu hình

- `.env.example`: full stack Docker/demo.
- `.env.backend.example`: backend deploy độc lập.
- `.env.prod.example`: production với Caddy/domain.
- `src/main/resources/application.yaml`: cấu hình chung và default không nhạy cảm.
- `src/main/resources/application-dev.yaml`: local development.
- `src/main/resources/application-docker.yaml`: full stack demo.
- `src/main/resources/application-prod.yaml`: production, yêu cầu secret từ environment.

Không commit `.env`, database dump, file upload runtime, token hoặc private key.

## Migration

Migration schema nằm trong `src/main/resources/db/migration`. Không sửa migration đã chạy; tạo version mới cho mọi thay đổi schema.

Dữ liệu demo nằm trong `src/main/resources/db/demo/R__demo_data.sql` và chỉ được cấu hình cho profile `dev/docker`.

## API và security

- Response JSON nghiệp vụ dùng `ApiResponseSever<T>`.
- Download/stream file trả binary trực tiếp và có `Content-Disposition` UTF-8.
- Swagger dùng bearer JWT ở profile dev/docker; production tắt Swagger.
- Access token 30 phút, refresh token 7 ngày theo default hiện tại.
- Refresh token rotation, session theo `jti`, logout và logout-all đều revoke session.

Xem thêm:

- [Triển khai](README_DEPLOY.md)
- [OpenAPI review](docs/12_phase_17_openapi_swagger.md)
- [Database review](docs/13_phase_17_database_review.md)
- [Final release checklist](docs/14_phase_17_final_release_checklist.md)
