# Phase 17 - Clean code audit

## Phạm vi đã kiểm tra

- Controller không truy cập repository trực tiếp.
- Service triển khai đầy đủ interface tương ứng.
- Không còn `System.out`, `System.err`, `printStackTrace`, `FIXME` hoặc debug marker trong mã nguồn chính.
- Không có lời gọi xóa cứng entity trong service; luồng xóa dự án tiếp tục dùng soft delete.
- Các native query còn lại chỉ phục vụ chức năng phụ thuộc PostgreSQL hoặc truy vấn báo cáo/cleanup chuyên biệt.
- `ErrorCode` không còn trùng mã số.
- Audit payload có trường nullable không còn dùng `Map.of`.
- Các helper private và repository method không có nơi sử dụng đã được loại bỏ.
- Quy tắc chuyển trạng thái Task dùng chung `TaskStatusTransitionValidator`; không duy trì hai bảng transition giống nhau.

## Native query được giữ lại có chủ đích

- `DatabaseLockRepository`: PostgreSQL advisory transaction lock cho concurrency.
- `AttachmentRepository`: lấy file đã soft-delete để cleanup và kiểm tra đường dẫn lưu trữ.
- `TaskTimeLogRepository`: báo cáo Excel theo khoảng ngày và nhiều bảng.

## Kế hoạch tách các service lớn

Không tách sâu trong bước này để tránh thay đổi transaction boundary, cache invalidation và phân quyền. Khi thực hiện ở bước riêng, ưu tiên theo thứ tự:

1. `TaskServiceImpl`: tách command cập nhật trạng thái, assignment, Kanban và mapper/audit payload.
2. `SprintServiceImpl`: tách lifecycle command, backlog synchronization và query mapper.
3. `TaskExcelTemplateServiceImpl` / `TaskExcelImportServiceImpl`: tách workbook formatting, row parser và validation lookup.
4. `ProjectReportServiceImpl` / `ProjectDashboardServiceImpl`: tách query aggregation khỏi response mapping.

Mỗi lần tách phải giữ nguyên API, propagation của transaction, cache key/evict và chạy lại toàn bộ integration test bằng Testcontainers.

## Xác minh

- `compileJava`: đạt.
- 77 unit test không phụ thuộc Docker: đạt, không có test fail hoặc skip.
- Nhóm 40 integration/repository/security test cần Docker/Testcontainers; lần chạy hiện tại không khởi tạo được Docker daemon nên chưa thể xác minh trên máy này.
- Frontend production build: đạt. ESLint hiện còn 111 lỗi và 3 cảnh báo tồn tại trên diện rộng; cần một bước frontend cleanup riêng để sửa an toàn, tránh trộn thay đổi UI/React vào đợt backend clean code này.
