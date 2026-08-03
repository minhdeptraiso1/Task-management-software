# Checklist kiểm thử API tích hợp

Checklist này dùng để theo dõi các luồng API quan trọng khi bảo trì hệ thống và trình bày đồ án.

## Xác thực và phiên đăng nhập

- Đăng nhập đúng thông tin trả access token và refresh token.
- Endpoint riêng tư từ chối request thiếu token, token sai hoặc token hết hạn.
- Refresh token được rotation; token cũ không thể dùng lại.
- Khóa tài khoản hoặc đổi role làm token đã cấp mất hiệu lực.
- Logout-all làm mất hiệu lực các phiên đăng nhập còn lại.
- Rate limit chặn lần đăng nhập vượt ngưỡng.

## Project và phân quyền

- Thành viên và ADMIN xem được dự án; người ngoài dự án bị chặn.
- ADMIN chỉ xem, không thao tác workflow dự án.
- Thành viên thuộc dự án, bao gồm DEVELOPER, được gán Task cho thành viên khác.
- Người ngoài dự án không được xem, gán hoặc di chuyển Task.

## Kanban

- Assignee được chuyển trạng thái Task hợp lệ.
- Chuyển `TODO -> IN_PROGRESS` thành công.
- Chuyển tắt trạng thái trái workflow bị chặn.
- Cập nhật vị trí đồng thời không làm trùng hoặc mất thứ tự.
- Sprint không ở trạng thái phù hợp sẽ không cho thao tác Kanban.

## Task comment

- Thành viên dự án được bình luận; người ngoài bị chặn.
- Chủ bình luận được sửa hoặc xóa; người khác bị chặn.
- Reply một cấp thành công, reply lồng sai quy tắc bị chặn.
- Xóa mềm, activity và notification được ghi đúng.

## Task time log

- Thành viên hợp lệ được ghi thời gian; người ngoài bị chặn.
- Không ghi thời gian cho Task đã hủy, ngày tương lai hoặc số phút không hợp lệ.
- Giới hạn tổng số phút trong ngày được áp dụng.
- Chỉ chủ time log được sửa hoặc xóa.
- Tổng hợp thời gian không tính bản ghi đã xóa.

## Notification ownership

- Người dùng chỉ xem, đánh dấu đã đọc và xóa notification của mình.
- Unread count chính xác.
- Notification của người khác trả `404` để không làm lộ dữ liệu.
- Actor không tự nhận notification và recipient không bị trùng.

## Attachment

- Người ngoài dự án không thể xem, tải lên hoặc tải attachment.
- Tên file nguy hiểm, extension bị cấm và path traversal bị chặn.
- File test chỉ được lưu trong `build/test-uploads`.

## Report

- Thành viên xem được JSON report, người ngoài bị chặn.
- Sprint, member và time report trả dữ liệu đúng phạm vi.
- Excel trả `.xlsx`, PDF trả `.pdf` và đúng content type.

## Atomic transaction

- Import có dòng lỗi rollback toàn bộ dữ liệu trong batch.
- Lỗi giữa transaction Sprint/Task không để lại dữ liệu dở dang.
- Sau rollback, trạng thái entity và quan hệ vẫn nhất quán.
