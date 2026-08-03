# Checklist kiểm thử phục vụ bảo vệ đồ án

## Nội dung cần trình bày

- [x] Mục tiêu kiểm thử và phạm vi Phase 15.
- [x] Môi trường Java 21, Spring Boot 3.5.1.
- [x] JUnit 5, Mockito, MockMvc và Testcontainers.
- [x] Unit test cho validation và business rule.
- [x] Repository test trên PostgreSQL thật.
- [x] Integration test cho luồng API chính.
- [x] Atomic test chứng minh transaction rollback.
- [x] Security test cho JWT, role, ownership và file.
- [x] Kết quả tổng: 113/113 test PASS.
- [ ] Chụp hình `build/reports/tests/test/index.html` để đưa vào báo cáo.

## Demo đề xuất khi bảo vệ

1. Chạy `./gradlew test` hoặc `.\gradlew.bat test`.
2. Mở HTML test report và chỉ ra tổng số test, số lỗi bằng 0.
3. Mở một unit test về chuyển trạng thái Kanban.
4. Mở một repository test chạy với PostgreSQL Testcontainer.
5. Mở `TaskImportAtomicIntegrationTest` để giải thích rollback toàn bộ batch.
6. Mở security test để giải thích outsider, ADMIN view-only và ownership.

## Câu hỏi thường gặp

### Vì sao không dùng H2?

PostgreSQL Testcontainer giúp kiểm tra đúng SQL, constraint, UUID, soft delete và hành vi transaction giống môi trường thật.

### Test có ảnh hưởng database dev không?

Không. Test profile nhận URL database động từ PostgreSQL container và dọn dữ liệu giữa các test.

### Test có phụ thuộc Redis cài trên máy không?

Không. Integration test dùng Redis container riêng; unit test mock Redis khi không cần kiểm tra kết nối thật.

### Làm sao chứng minh transaction rollback?

Atomic test chủ động tạo lỗi giữa transaction, sau đó kiểm tra database không còn dữ liệu được ghi một phần.

### Chính sách gán Task hiện tại là gì?

Mọi thành viên thuộc dự án, bao gồm DEVELOPER, được gán Task cho thành viên khác. Người ngoài dự án và ADMIN view-only bị chặn.

## Kiểm tra trước khi nộp

- [ ] Docker đang chạy để Testcontainers khởi tạo PostgreSQL/Redis.
- [ ] `clean test` chạy thành công từ workspace sạch.
- [ ] Không có file upload test nằm ngoài `build/test-uploads`.
- [ ] Không commit `.env`, database dump hoặc token thật.
- [ ] Report và ảnh chụp test pass khớp cùng một lần chạy.
