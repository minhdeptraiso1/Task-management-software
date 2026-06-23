# 📚 Tài Liệu Dự Án — Base Java Spring Boot

> Đọc các file theo thứ tự dưới đây để hiểu toàn bộ dự án từ tổng quan đến chi tiết.

---

## 📂 Danh Sách Tài Liệu

| File | Nội dung |
|------|---------|
| [01_project_overview.md](./01_project_overview.md) | Tổng quan dự án, công nghệ sử dụng, cách chạy |
| [02_project_structure.md](./02_project_structure.md) | Cấu trúc thư mục, vai trò từng package |
| [03_coding_conventions.md](./03_coding_conventions.md) | Quy tắc code: DTO dùng Record, Enum ErrorCode, Lombok... |
| [04_security.md](./04_security.md) | JWT, Token Blacklist, Rate Limiting, phân quyền |
| [05_error_handling.md](./05_error_handling.md) | ErrorCode enum, GlobalExceptionHandler, cách throw lỗi |
| [06_cache.md](./06_cache.md) | Redis Cache, TTL, @Cacheable/@CacheEvict/@CachePut |
| [07_database.md](./07_database.md) | Entity, Flyway migration, Soft Delete, JPA Auditing |
| [08_api_reference.md](./08_api_reference.md) | Danh sách API, request/response mẫu |

---

## 🚀 Quick Start

```bash
# 1. Copy file env
cp .env.example .env
# Điền thông tin DB, Redis, JWT vào .env

# 2. Khởi động dependencies (PostgreSQL + Redis)
docker-compose up -d

# 3. Chạy ứng dụng
./gradlew bootRun
```

**Swagger UI:** `http://localhost:8080/api/swagger`

---

## 🛠 Tech Stack

| Công nghệ | Version | Mục đích |
|-----------|---------|---------|
| Java | 21 | Ngôn ngữ chính |
| Spring Boot | 3.5.x | Framework |
| PostgreSQL | 15+ | Database chính |
| Redis | 7+ | Cache + Token Blacklist |
| Flyway | - | Database migration |
| MapStruct | 1.6.3 | Object mapping |
| Lombok | - | Boilerplate reduction |
| SpringDoc OpenAPI | 2.x | Swagger UI |
