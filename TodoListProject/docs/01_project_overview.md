# 01 — Tổng Quan Dự Án

## Mục Đích

**Base Java Spring Boot** là project nền tảng (boilerplate) chuẩn hóa cho các dự án backend Java, bao gồm sẵn:

- ✅ Authentication & Authorization (JWT)
- ✅ Redis Cache
- ✅ Token Blacklist & Rate Limiting
- ✅ Soft Delete + JPA Auditing
- ✅ Global Exception Handling
- ✅ Audit Log + WebSocket realtime
- ✅ Swagger UI
- ✅ Flyway Database Migration
- ✅ Cấu hình qua `.env`

---

## Công Nghệ Sử Dụng

```
Java 21
Spring Boot 3.5.x
├── spring-boot-starter-web          → REST API
├── spring-boot-starter-security     → Spring Security
├── spring-boot-starter-data-jpa     → JPA/Hibernate
├── spring-boot-starter-data-redis   → Redis
├── spring-boot-starter-websocket    → WebSocket/STOMP
├── spring-boot-starter-validation   → Bean Validation
├── spring-boot-starter-actuator     → Health + Metrics
└── spring-boot-starter-oauth2-resource-server → JWT

PostgreSQL 15+          → Database chính
Redis 7+                → Cache + Token blacklist
Flyway                  → Database migration
MapStruct 1.6.3         → DTO ↔ Entity mapping
Lombok                  → Boilerplate reduction
springdoc-openapi 2.x   → Swagger UI
spring-dotenv 4.0.0     → Load biến từ file .env
```

---

## Cách Chạy Local

### Bước 1 — Tạo file `.env`

```bash
cp .env.example .env
```

Sau đó chỉnh sửa `.env`:

```env
DB_URL=jdbc:postgresql://localhost:5432/app_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your_secret_key_minimum_32_characters
JWT_ACCESS_EXPIRATION=900        # giây (15 phút)
JWT_REFRESH_EXPIRATION=7d        # ngày

CACHE_DEFAULT_TTL=10m
CACHE_TTL_USER_DETAIL=10m
CACHE_TTL_USER_CURRENT=5m
CACHE_TTL_USER_SEARCH=2m

CONTEXT_PATH=/api
```

### Bước 2 — Khởi động PostgreSQL + Redis

```bash
docker-compose up -d
```

### Bước 3 — Chạy ứng dụng

```bash
./gradlew bootRun
```

### Bước 4 — Truy cập Swagger UI

```
http://localhost:8080/api/swagger
```

---

## Tài Khoản Mặc Định (Seed Data)

| Field | Giá trị |
|-------|---------|
| Email | `admin@example.com` |
| Password | `123456` |
| Role | `ADMIN` |

> Seed data được chạy tự động qua Flyway migration `V2__data.sql`.

---

## Build & Test

```bash
# Chạy unit/integration test
./gradlew test

# Build JAR
./gradlew build

# Chạy JAR
java -jar build/libs/*.jar
```
