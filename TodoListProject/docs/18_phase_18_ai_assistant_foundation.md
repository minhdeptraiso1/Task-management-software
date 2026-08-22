# Phase 18 - AI Assistant foundation

Backend cung cấp `POST /api/projects/{projectId}/ai/ask` với body `{ "question": "..." }`.
API chỉ cho người dùng có quyền xem project, chỉ gửi context tổng hợp của project và không thực hiện thao tác ghi.

## Cấu hình

Đặt `GEMINI_API_KEY` bằng secret của môi trường chạy. Có thể đổi `GEMINI_MODEL`, `GEMINI_BASE_URL` và `GEMINI_TIMEOUT_SECONDS`. Không commit API key vào Git; nếu key từng bị dán công khai, cần revoke/rotate.

Khi chưa có key, ứng dụng vẫn khởi động bình thường và API trả lỗi cấu hình rõ ràng.

## Phase 18 bước 2: Meeting suggestions

Endpoint `POST /api/projects/{projectId}/ai/meeting-suggestions` nhận:

```json
{"meetingType":"DAILY","additionalNote":"Tập trung vào blocker và task quá hạn"}
```

Các loại meeting gồm `DAILY`, `SPRINT_PLANNING`, `SPRINT_REVIEW`, `RETROSPECTIVE` và `ISSUE_RESOLUTION`. Response có title, purpose, agenda, câu hỏi thảo luận, rủi ro và task liên quan. AI chỉ gợi ý; backend không tạo meeting, task, lịch hay gửi email.

## Phase 18 bước 3: Meeting minutes

Endpoint `POST /api/projects/{projectId}/ai/meeting-minutes` nhận ghi chú thô và trả biên bản gồm tổng quan, nội dung thảo luận, quyết định, vấn đề còn tồn đọng, rủi ro và `actionItemDrafts`. Các action item chỉ là bản nháp để frontend hiển thị, chưa tạo task hay cập nhật database.

## Phase 18 bước 4: Action item suggestions

Endpoint `POST /api/projects/{projectId}/ai/action-items` phân tích nội dung họp và trả tối đa 10 action item nháp. Người dùng phải kiểm tra lại trước khi tạo task; bước này chưa tự động ghi task vào database.

## Phase 18 bước 5: Hỏi đáp dữ liệu project

Endpoint `/api/projects/{projectId}/ai/ask` trả JSON gồm `answer`, `keyPoints`, `relatedTasks`, `risks`, `suggestions` và `limitation`. Người dùng có thể gửi thêm `additionalContext`; AI chỉ diễn giải dữ liệu project hiện tại và không thực hiện thao tác ghi.

## Phase 18 bước 6: Quản lý meeting và link Google Meet

Đã thêm quản lý meeting theo project với các API tạo, danh sách, chi tiết và cập nhật link Google Meet. Bước này chỉ lưu link `https://meet.google.com/...` do người dùng nhập, chưa tích hợp Google Calendar/OAuth và chưa tự động tạo cuộc họp.

## Dữ liệu audit

Migration `V24__create_ai_request_logs.sql` lưu câu hỏi, câu trả lời, provider/model, người dùng, project và trạng thái thành công. Không lưu prompt nội bộ chứa toàn bộ context để hạn chế dữ liệu nhạy cảm.
