# Phase 17 bước 4 — OpenAPI/Swagger

## Địa chỉ sử dụng

Backend có context path mặc định `/api`, vì vậy khi chạy profile `dev`:

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

Profile `prod` tắt cả Swagger UI và OpenAPI JSON. Profile `dev` và `docker` bật tài liệu API.

## Xác thực Bearer JWT

1. Gọi `POST /auth/login` để nhận `accessToken`.
2. Chọn **Authorize** trên Swagger UI.
3. Nhập access token. Swagger tự thêm tiền tố `Bearer`.
4. API `login` và `refresh` được đánh dấu là public; các API nghiệp vụ dùng security scheme `bearerAuth`.

## Response JSON thống nhất

API JSON trả wrapper:

```json
{
  "code": 1000,
  "message": "Thành công",
  "data": {},
  "timestamp": "2026-08-04T10:00:00+07:00",
  "path": "/api/projects"
}
```

Các nhóm lỗi phổ biến:

- `400`: request hoặc quy tắc nghiệp vụ không hợp lệ.
- `401`: chưa đăng nhập, token hết hạn hoặc token không hợp lệ.
- `403`: không có quyền thực hiện.
- `404`: không tìm thấy tài nguyên.
- `409`: xung đột dữ liệu hoặc trạng thái.
- `429`: vượt giới hạn tần suất.
- `500`: lỗi nội bộ hệ thống.

Danh sách numeric error code chi tiết nằm tại [error-code-reference.md](error-code-reference.md).

## Phân trang

Các API danh sách dùng query parameter chuẩn Spring:

- `page`: số trang, bắt đầu từ `0`.
- `size`: số phần tử mỗi trang.
- `sort`: `field,direction`, ví dụ `createdAt,desc`.

Các request filter và `Pageable` được hiển thị dưới dạng query parameter nhờ `@ParameterObject`.

## Upload và download file

- Upload attachment và import Task Excel dùng `multipart/form-data`, field file có tên `file`.
- Excel, PDF và attachment download trả nội dung nhị phân trực tiếp bằng `ResponseEntity`, không bọc `ApiResponseSever`.
- Tên file tải về đọc từ header `Content-Disposition`.
- Attachment download luôn kiểm tra quyền Project; không public thư mục lưu file.

## Nhóm API

Controller được phân theo các tag chuẩn trong `OpenApiTags`: Authentication, Users, Projects, Project Members, Sprints, Product Backlog, Tasks, Kanban, Task Comments, Time Tracking, Notifications, Project Activities, Dashboard, Reports, Attachments, Search, Bugs / QA, Administration và System Audit.

## Kiểm thử tự động

`OpenApiDocumentationTest` kiểm tra:

- metadata và bearer JWT trong `OpenApiConfig`;
- mọi REST controller có `@Tag`;
- controller cần đăng nhập có `@SecurityRequirement`;
- mọi mapped endpoint có `@Operation`;
- endpoint upload khai báo multipart và nhận `MultipartFile`;
- endpoint tải attachment/Excel/PDF giữ response file nhị phân và có tài liệu binary response.
