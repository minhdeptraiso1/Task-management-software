# Phase 17 - Bước 2: Response Consistency

## Cấu trúc JSON thống nhất

Mọi API JSON thành công và thất bại đều dùng `ApiResponseSever<T>`:

```json
{
  "code": 1000,
  "message": "Thành công",
  "data": {},
  "timestamp": "2026-08-04T11:55:00Z",
  "path": null
}
```

Lỗi validation dùng mã `422000`; `data.errors` chứa toàn bộ lỗi field, được sắp xếp theo tên field. Các lỗi nghiệp vụ, request, security, database và lỗi hệ thống đều có `timestamp` và `path`. Chi tiết SQL và stack trace chỉ được ghi vào log server.

## Security

`RestAuthenticationEntryPoint`, `RestAccessDeniedHandler` và lỗi JWT phát sinh trong filter dùng cùng cấu trúc response. Vì lỗi trong filter không đi qua `GlobalExceptionHandler`, các class này tự ghi JSON chuẩn và luôn kèm request path.

## Pagination và file

- 20 DTO phân trang đã được rà soát và đều có: `content`, `totalElements`, `totalPages`, `number`, `size`, `numberOfElements`, `first`, `last`, `empty`.
- API Excel, PDF và attachment tiếp tục trả `ResponseEntity<byte[]>` hoặc `ResponseEntity<Resource>`; không bọc trong JSON.
- Controller không trả `Map`, `String` hoặc `Object` rời rạc cho API JSON.

## Frontend

API client đọc response mới bằng `code === 1000`, tự lấy `message`, `code` và ghép đầy đủ danh sách `data.errors` để popup có thể hiển thị lỗi validation rõ ràng. Logic đọc response cũ được giữ tạm thời chỉ ở nhánh xử lý lỗi để hỗ trợ môi trường đang chuyển đổi.

## Kiểm thử

Đã bổ sung test cho:

- success response và `withPath`;
- dynamic business message;
- danh sách field validation và thứ tự ổn định;
- JSON 401/403 từ Spring Security;
- cập nhật assertion integration test theo response mới.
