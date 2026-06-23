# Base Spring Boot Application v1

## 📋 Mục lục
- [Giới thiệu](#giới-thiệu)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt và chạy ứng dụng](#cài-đặt-và-chạy-ứng-dụng)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Tính năng chính](#tính-năng-chính)
- [API Documentation](#api-documentation)
- [Cấu hình](#cấu-hình)
- [Database](#database)
- [Testing](#testing)

---

## 🎯 Giới thiệu

Đây là một ứng dụng Spring Boot cơ bản được xây dựng làm nền tảng cho các dự án Java backend. Ứng dụng cung cấp các chức năng cơ bản như:
- Xác thực và phân quyền người dùng (JWT Authentication)
- Quản lý người dùng với phân quyền ROLE-based
- Audit logging để theo dõi hoạt động
- WebSocket để giao tiếp real-time
- RESTful API với OpenAPI/Swagger documentation

## 🚀 Công nghệ sử dụng

### Core Framework
- **Java 21** - Programming language
- **Spring Boot 3.5.1** - Application framework
- **Gradle** - Build tool

### Spring Modules
- **Spring Web** - RESTful API
- **Spring Data JPA** - Database persistence
- **Spring Security** - Authentication & Authorization
- **Spring WebSocket** - Real-time communication
- **Spring Actuator** - Application monitoring

### Database & Cache
- **PostgreSQL** - Primary database
- **Redis** - Caching & session management
- **Flyway** - Database migration
- **H2** - In-memory database for testing

### Security
- **JWT (JSON Web Token)** - Token-based authentication
- **OAuth2 Resource Server** - JWT validation
- **BCrypt** - Password encryption

### Tools & Libraries
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **SpringDoc OpenAPI** - API documentation
- **Micrometer Prometheus** - Metrics collection

### DevOps
- **Docker Compose** - Container orchestration
- **Spring DevTools** - Hot reload during development

---

## 💻 Yêu cầu hệ thống

Trước khi chạy ứng dụng, bạn cần cài đặt:

1. **Java Development Kit (JDK) 21 hoặc cao hơn**
   - Download: https://adoptium.net/
   - Kiểm tra: `java -version`

2. **Docker & Docker Compose** (cho PostgreSQL và Redis)
   - Download: https://www.docker.com/products/docker-desktop
   - Kiểm tra: `docker --version` và `docker-compose --version`

3. **IDE** (khuyến nghị)
   - IntelliJ IDEA (Ultimate hoặc Community)
   - VS Code với Java Extension Pack

---

## 🔧 Cài đặt và chạy ứng dụng

### Bước 1: Clone dự án
```bash
cd d:\Do_An_Tot_Nghiep\base_spring_boot\base_v1
```

### Bước 2: Khởi động Database và Redis
```bash
docker-compose up -d
```

Lệnh này sẽ khởi động:
- PostgreSQL trên cổng `5432`
- Redis trên cổng `6379`

Kiểm tra containers đang chạy:
```bash
docker-compose ps
```

### Bước 3: Build ứng dụng
```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### Bước 4: Chạy ứng dụng

**Cách 1: Sử dụng Gradle**
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

**Cách 2: Chạy JAR file**
```bash
java -jar build/libs/base_v1-0.0.1-SNAPSHOT.jar
```

**Cách 3: Sử dụng IDE**
- Mở dự án trong IntelliJ IDEA hoặc VS Code
- Chạy class `BaseV1Application.java`

### Bước 5: Kiểm tra ứng dụng đã chạy

Mở trình duyệt và truy cập:
- **Swagger UI**: http://localhost:8080/swagger
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

---

## 📁 Cấu trúc dự án

```
base_v1/
├── src/
│   ├── main/
│   │   ├── java/com/project/base_v1/
│   │   │   ├── BaseV1Application.java          # Main application class
│   │   │   ├── config/                         # Cấu hình ứng dụng
│   │   │   │   ├── JpaAuditConfig.java        # JPA auditing config
│   │   │   │   ├── OpenApiConfig.java         # Swagger/OpenAPI config
│   │   │   │   ├── RedisConfig.java           # Redis configuration
│   │   │   │   └── WebSocketConfig.java       # WebSocket configuration
│   │   │   │
│   │   │   ├── controller/                     # REST API Controllers
│   │   │   │   ├── AuthController.java        # Authentication endpoints
│   │   │   │   └── UserController.java        # User management endpoints
│   │   │   │
│   │   │   ├── dto/                           # Data Transfer Objects
│   │   │   │   ├── request/                   # Request DTOs
│   │   │   │   │   ├── auth/                  # Login request, etc.
│   │   │   │   │   └── user/                  # User CRUD requests
│   │   │   │   └── response/                  # Response DTOs
│   │   │   │       ├── auth/                  # Auth response with tokens
│   │   │   │       ├── user/                  # User response
│   │   │   │       └── core/                  # Standard API response
│   │   │   │
│   │   │   ├── entity/                        # JPA Entities
│   │   │   │   ├── User.java                  # User entity
│   │   │   │   ├── TokenSession.java          # Refresh token storage
│   │   │   │   ├── AuditLog.java              # Activity logging
│   │   │   │   └── BaseAuditEntity.java       # Base entity with audit fields
│   │   │   │
│   │   │   ├── enums/                         # Enumerations
│   │   │   │   └── UserRole.java              # ADMIN, USER roles
│   │   │   │
│   │   │   ├── exception/                      # Exception handling
│   │   │   │   └── GlobalExceptionHandler.java # Centralized error handling
│   │   │   │
│   │   │   ├── mapper/                        # MapStruct mappers
│   │   │   │   └── UserMapper.java            # Entity <-> DTO conversion
│   │   │   │
│   │   │   ├── repository/                    # Data Access Layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TokenSessionRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   │
│   │   │   ├── security/                      # Security components
│   │   │   │   ├── SecurityConfig.java        # Spring Security configuration
│   │   │   │   ├── JwtAuthenticationFilter.java # JWT filter
│   │   │   │   ├── JwtTokenProvider.java      # JWT generation/validation
│   │   │   │   ├── TokenBlacklistService.java # Revoke tokens
│   │   │   │   ├── LoginRateLimiter.java      # Prevent brute force
│   │   │   │   └── CustomUserDetailsService.java # Load user for auth
│   │   │   │
│   │   │   ├── service/                       # Business Logic Layer
│   │   │   │   ├── AuthService.java           # Authentication service interface
│   │   │   │   ├── UserService.java           # User service interface
│   │   │   │   ├── AuditLogService.java       # Audit logging interface
│   │   │   │   └── impl/                      # Service implementations
│   │   │   │
│   │   │   └── util/                          # Utility classes
│   │   │
│   │   └── resources/
│   │       ├── application.yaml               # Main configuration
│   │       └── db/migration/                  # Flyway migrations
│   │           ├── V1__init_schema.sql        # Initial schema
│   │           └── V2__data.sql               # Seed data (admin user)
│   │
│   └── test/                                  # Test classes
│       ├── java/
│       │   └── com/project/base_v1/
│       │       ├── AuthControllerTest.java
│       │       └── BaseV1ApplicationTests.java
│       └── resources/
│           └── application-test.yaml          # Test configuration
│
├── build.gradle                               # Gradle dependencies
├── settings.gradle                            # Gradle settings
├── docker-compose.yaml                        # Docker services
└── README.md                                  # This file
```

---

## ✨ Tính năng chính

### 1. **Authentication & Authorization** (Xác thực và Phân quyền)

#### JWT-based Authentication
- Access Token: có thời hạn 15 phút (900 giây)
- Refresh Token: có thời hạn 7 ngày
- Token được lưu trong Redis cho việc blacklist khi logout

#### Endpoints:
```
POST /auth/login          - Đăng nhập (public)
POST /auth/refresh        - Làm mới access token
POST /auth/logout         - Đăng xuất
```

#### Role-based Access Control
- `ADMIN`: Toàn quyền quản lý hệ thống
- `USER`: Người dùng thông thường

### 2. **User Management** (Quản lý người dùng)

#### Endpoints:
```
GET  /users/me            - Lấy thông tin user hiện tại
GET  /users/search        - Tìm kiếm users (ADMIN)
GET  /users/{id}          - Xem chi tiết user
POST /users               - Tạo user mới (ADMIN)
PUT  /users/{id}          - Cập nhật user (ADMIN)
DELETE /users/{id}        - Xóa user (soft delete) (ADMIN)
```

#### Features:
- Soft delete (không xóa vĩnh viễn)
- Tìm kiếm theo username, email, role, status
- Phân trang và sắp xếp kết quả
- Audit tracking (created_at, updated_at, created_by, updated_by)

### 3. **Audit Logging** (Ghi nhận hoạt động)

Tự động ghi lại các hoạt động quan trọng:
- User login
- User logout
- User creation
- User update
- User deletion

Logs được lưu trong database và có thể push qua WebSocket để theo dõi real-time.

### 4. **WebSocket** (Giao tiếp real-time)

Cấu hình STOMP protocol:
- **Endpoint**: `/ws` (với SockJS fallback)
- **Subscribe to**: `/topic/audit-logs`
- **Application prefix**: `/app`

Client có thể kết nối và nhận thông báo real-time về các hoạt động trong hệ thống.

### 5. **API Documentation** (Tài liệu API)

Swagger/OpenAPI tích hợp sẵn:
- URL: http://localhost:8080/swagger
- Có thể test trực tiếp các API
- Hỗ trợ JWT Bearer authentication

### 6. **Security Features** (Tính năng bảo mật)

- ✅ Password encryption với BCrypt
- ✅ JWT token với signature verification
- ✅ Token blacklist khi logout
- ✅ Rate limiting cho login để chống brute force
- ✅ CORS configuration
- ✅ Stateless session management

### 7. **Database Migration** (Quản lý schema)

Sử dụng Flyway để version control database:
- `V1__init_schema.sql`: Tạo tables
- `V2__data.sql`: Insert dữ liệu mẫu

Migration tự động chạy khi start application.

### 8. **Monitoring & Metrics** (Giám sát)

Spring Actuator endpoints:
- `/actuator/health` - Health check
- `/actuator/prometheus` - Metrics cho Prometheus
- `/actuator/info` - Application info

---

## 📖 API Documentation

### Authentication

#### 1. Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "error": null
}
```

#### 2. Refresh Token
```http
POST /auth/refresh?refreshToken=<your-refresh-token>

Response:
{
  "success": true,
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

#### 3. Logout
```http
POST /auth/logout?refreshToken=<refresh-token>
Authorization: Bearer <access-token>

Response:
{
  "success": true,
  "data": null,
  "error": null
}
```

### User Management

#### 1. Get Current User
```http
GET /users/me
Authorization: Bearer <access-token>

Response:
{
  "success": true,
  "data": {
    "id": "uuid",
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN",
    "enabled": true
  }
}
```

#### 2. Search Users (ADMIN only)
```http
GET /users/search?keyword=admin&role=ADMIN&enabled=true&page=0&size=10
Authorization: Bearer <access-token>

Response:
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 10,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

#### 3. Create User (ADMIN only)
```http
POST /users
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "role": "USER"
}
```

---

## ⚙️ Cấu hình

### application.yaml

File cấu hình chính nằm tại: `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/appdb
    username: postgres
    password: 081003

  jpa:
    hibernate:
      ddl-auto: validate          # Không tự động sửa schema
    open-in-view: false           # Tắt OSIV pattern

  flyway:
    baseline-on-migrate: true     # Cho phép migrate trên DB có sẵn

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  access-token-expiration: 900    # 15 phút
  refresh-token-expiration: 7d    # 7 ngày
  secret: 9f3a8d7c6e5b4a3928f7e6d5c4b3a291  # Đổi secret này trong production!

springdoc:
  swagger-ui:
    path: /swagger
```

### Môi trường Production

**⚠️ QUAN TRỌNG**: Trước khi deploy production, bạn cần:

1. **Thay đổi JWT Secret**: Tạo secret key mạnh hơn
   ```bash
   # Generate random secret
   openssl rand -base64 32
   ```

2. **Cấu hình Database**: Sử dụng database riêng cho production
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://production-host:5432/proddb
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
   ```

3. **Environment Variables**: Sử dụng biến môi trường cho sensitive data
   ```bash
   export DB_PASSWORD=your-secure-password
   export JWT_SECRET=your-jwt-secret
   ```

4. **Disable Swagger trong production**:
   ```yaml
   springdoc:
     swagger-ui:
       enabled: false
   ```

---

## 💾 Database

### PostgreSQL Schema

#### Table: users
```sql
Column      | Type         | Description
------------|--------------|---------------------------
id          | UUID         | Primary key
username    | VARCHAR(100) | Unique username
email       | VARCHAR(255) | Unique email
password    | VARCHAR(255) | BCrypt hashed password
role        | VARCHAR(50)  | ADMIN or USER
enabled     | BOOLEAN      | Account status
created_at  | TIMESTAMP    | Creation time
updated_at  | TIMESTAMP    | Last update time
created_by  | VARCHAR(100) | Creator username
updated_by  | VARCHAR(100) | Last updater username
deleted_at  | TIMESTAMP    | Soft delete timestamp
deleted_by  | VARCHAR(100) | Deleter username
```

#### Table: token_sessions
Lưu refresh tokens:
```sql
Column        | Type      | Description
--------------|-----------|---------------------------
id            | UUID      | Primary key
user_id       | UUID      | Foreign key to users
refresh_token | TEXT      | Refresh token value
revoked       | BOOLEAN   | Token revocation status
expired_at    | TIMESTAMP | Token expiration time
```

#### Table: audit_logs
Ghi lại hoạt động:
```sql
Column     | Type         | Description
-----------|--------------|---------------------------
id         | UUID         | Primary key
user_id    | UUID         | User who performed action
action     | VARCHAR(255) | Action description
created_at | TIMESTAMP    | Action timestamp
```

### Default User

Sau khi chạy migration, hệ thống có sẵn 1 user admin:
```
Username: admin
Password: 123456
Email: admin@example.com
Role: ADMIN
```

**⚠️ Đổi password này ngay sau khi login lần đầu!**

### Redis

Redis được sử dụng để:
- Lưu blacklist tokens (khi logout)
- Cache dữ liệu (có thể mở rộng)
- Rate limiting

---

## 🧪 Testing

### Chạy Tests

```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

### Test Report

Sau khi chạy tests, xem report tại:
```
build/reports/tests/test/index.html
```

### Test Configuration

Tests sử dụng:
- H2 in-memory database (không cần PostgreSQL)
- Configuration riêng: `src/test/resources/application-test.yaml`

### Existing Tests

1. **AuthControllerTest**: Test authentication endpoints
2. **BaseV1ApplicationTests**: Context loading test

---

## 🐛 Troubleshooting (Xử lý lỗi)

### Lỗi: "Connection refused" khi connect database
**Nguyên nhân**: PostgreSQL hoặc Redis chưa chạy

**Giải pháp**:
```bash
docker-compose up -d
docker-compose ps  # Kiểm tra containers
```

### Lỗi: "Port 8080 already in use"
**Nguyên nhân**: Có ứng dụng khác đang dùng port 8080

**Giải pháp**: Thay đổi port trong `application.yaml`:
```yaml
server:
  port: 8081
```

### Lỗi: "JWT signature does not match"
**Nguyên nhân**: JWT secret không khớp hoặc token đã expire

**Giải pháp**: 
- Login lại để lấy token mới
- Kiểm tra JWT secret trong config

### Lỗi: Flyway migration failed
**Nguyên nhân**: Database schema không khớp với migration

**Giải pháp**:
```bash
# Xóa database và tạo lại
docker-compose down -v
docker-compose up -d
```

---

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [Flyway](https://flywaydb.org/)
- [Redis](https://redis.io/)
- [PostgreSQL](https://www.postgresql.org/)

---

## 📞 Hỗ trợ

Nếu gặp vấn đề, bạn có thể:
1. Kiểm tra logs trong console
2. Xem actuator health: http://localhost:8080/actuator/health
3. Kiểm tra Docker containers: `docker-compose ps`
4. Xem logs của containers: `docker-compose logs`

---

## 📝 License

This project is for educational purposes.

---

**Happy Coding! 🎉**
