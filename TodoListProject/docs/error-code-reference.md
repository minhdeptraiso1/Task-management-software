# Error Code Reference

Tài liệu này là nguồn tra cứu mã lỗi công khai của API HICAS. Danh sách được sinh từ `ErrorCode.java` sau đợt rà soát Phase 17 bước 3.

## Quy ước

| Nhóm mã | Ý nghĩa |
|---:|---|
| 400xxx | Request hoặc quy tắc nghiệp vụ không hợp lệ |
| 401xxx | Chưa xác thực hoặc token không hợp lệ |
| 403xxx | Đã xác thực nhưng không đủ quyền |
| 404xxx | Không tìm thấy tài nguyên hoặc cố ý ẩn tài nguyên |
| 409xxx | Xung đột dữ liệu hoặc trạng thái |
| 415xxx | Loại nội dung không được hỗ trợ |
| 422xxx | Validation chi tiết theo field/import |
| 429xxx | Vượt giới hạn tần suất |
| 500xxx | Lỗi nội bộ; chi tiết chỉ ghi trong log server |

- Không đổi numeric code đang được client sử dụng nếu chưa có kế hoạch migration.
- Notification của người khác trả 404 để tránh lộ tài nguyên.
- Lỗi validation DTO dùng `VALIDATION_FAILED` và trả danh sách `data.errors`.
- API file chỉ trả JSON ErrorCode khi thất bại; khi thành công trả file nhị phân.

## Checklist theo module

| Module | Số mã | Trạng thái rà soát |
|---|---:|---|
| Common / Validation | 11 | Đã rà soát |
| Auth / Token | 12 | Đã rà soát |
| User | 7 | Đã rà soát |
| Project | 10 | Đã rà soát |
| Project Member | 6 | Đã rà soát |
| Backlog Item | 10 | Đã rà soát |
| Sprint | 19 | Đã rà soát |
| Task / Kanban | 22 | Đã rà soát |
| Task Dependency | 5 | Đã rà soát |
| Task Comment | 6 | Đã rà soát |
| Task Time Log | 10 | Đã rà soát |
| Task Import / Excel | 12 | Đã rà soát |
| Bug / QA | 22 | Đã rà soát |
| Notification | 1 | Đã rà soát |
| Report / Dashboard | 7 | Đã rà soát |
| Attachment / File / Filter | 18 | Đã rà soát |
| Admin / Audit / Activity | 3 | Đã rà soát |
| Rate Limit | 2 | Đã rà soát |
| Infrastructure | 5 | Đã rà soát |

## Danh mục đầy đủ

| Code | HTTP Status | Name | Message | Module |
|---:|---|---|---|---|
| 400002 | BAD_REQUEST | `INVALID_REQUEST_BODY` | Dữ liệu gửi lên không hợp lệ | Common / Validation |
| 400004 | BAD_REQUEST | `MISSING_REQUIRED_FIELD` | Thiếu thông tin bắt buộc | Common / Validation |
| 400005 | BAD_REQUEST | `INVALID_PARAMETER` | Tham số không hợp lệ | Common / Validation |
| 400006 | BAD_REQUEST | `INVALID_ENUM_VALUE` | Giá trị enum không hợp lệ | Common / Validation |
| 400008 | BAD_REQUEST | `INVALID_UUID_FORMAT` | Định dạng UUID không hợp lệ | Common / Validation |
| 405001 | METHOD_NOT_ALLOWED | `METHOD_NOT_ALLOWED` | Phương thức HTTP không được hỗ trợ | Common / Validation |
| 409003 | CONFLICT | `DATA_ALREADY_EXISTS` | Dữ liệu đã tồn tại | Common / Validation |
| 409004 | CONFLICT | `DATA_INTEGRITY_VIOLATION` | Dữ liệu bị trùng hoặc vi phạm ràng buộc | Common / Validation |
| 409005 | CONFLICT | `FOREIGN_KEY_VIOLATION` | Dữ liệu liên kết không tồn tại hoặc không hợp lệ | Common / Validation |
| 415001 | UNSUPPORTED_MEDIA_TYPE | `UNSUPPORTED_MEDIA_TYPE` | Định dạng dữ liệu không được hỗ trợ | Common / Validation |
| 422000 | UNPROCESSABLE_ENTITY | `VALIDATION_FAILED` | Dữ liệu đầu vào không hợp lệ | Common / Validation |
| 400009 | BAD_REQUEST | `CURRENT_PASSWORD_INVALID` | Mật khẩu hiện tại không chính xác | Auth / Token |
| 400010 | BAD_REQUEST | `PASSWORD_CONFIRM_NOT_MATCH` | Mật khẩu xác nhận không khớp | Auth / Token |
| 400011 | BAD_REQUEST | `NEW_PASSWORD_SAME_AS_CURRENT` | Mật khẩu mới không được trùng với mật khẩu hiện tại | Auth / Token |
| 401001 | UNAUTHORIZED | `INVALID_CREDENTIALS` | Tên đăng nhập hoặc mật khẩu không đúng | Auth / Token |
| 401002 | UNAUTHORIZED | `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Auth / Token |
| 401003 | UNAUTHORIZED | `TOKEN_REVOKED` | Token đã bị thu hồi | Auth / Token |
| 401005 | UNAUTHORIZED | `UNAUTHENTICATED` | Bạn chưa đăng nhập | Auth / Token |
| 401006 | UNAUTHORIZED | `REFRESH_TOKEN_EXPIRED` | Refresh token đã hết hạn | Auth / Token |
| 401007 | UNAUTHORIZED | `REFRESH_TOKEN_INVALID` | Refresh token không hợp lệ | Auth / Token |
| 401008 | UNAUTHORIZED | `INVALID_ACCESS_TOKEN` | Access token không hợp lệ | Auth / Token |
| 403001 | FORBIDDEN | `ACCESS_DENIED` | Bạn không có quyền thực hiện chức năng này | Auth / Token |
| 403002 | FORBIDDEN | `ACCOUNT_DISABLED` | Tài khoản đã bị khóa | Auth / Token |
| 400110 | BAD_REQUEST | `USER_CANNOT_DISABLE_SELF` | Không thể disable chính tài khoản đang đăng nhập | User |
| 400111 | BAD_REQUEST | `USER_CANNOT_DISABLE_LAST_ADMIN` | Không thể disable ADMIN cuối cùng của hệ thống | User |
| 400112 | BAD_REQUEST | `USER_CANNOT_DEMOTE_LAST_ADMIN` | Không thể hạ quyền ADMIN cuối cùng của hệ thống | User |
| 404001 | NOT_FOUND | `USER_NOT_FOUND` | Không tìm thấy người dùng | User |
| 409001 | CONFLICT | `USERNAME_ALREADY_EXISTS` | Tên đăng nhập đã tồn tại | User |
| 409002 | CONFLICT | `EMAIL_ALREADY_EXISTS` | Email đã tồn tại | User |
| 409110 | CONFLICT | `USER_ROLE_CONFLICT_WITH_PROJECT_ROLE` | System role mới không phù hợp với vai trò hiện tại trong Project | User |
| 400012 | BAD_REQUEST | `PROJECT_DATE_INVALID` | Ngày kết thúc Project không được trước ngày bắt đầu | Project |
| 400013 | BAD_REQUEST | `PROJECT_NAME_REQUIRED` | Tên Project không được để trống | Project |
| 403005 | FORBIDDEN | `PROJECT_CREATE_FORBIDDEN` | Chỉ tài khoản quản lý mới được tạo dự án | Project |
| 403006 | FORBIDDEN | `PROJECT_ACCESS_DENIED` | Bạn không có quyền truy cập dự án này | Project |
| 403008 | FORBIDDEN | `PROJECT_UPDATE_DENIED` | Bạn không có quyền cập nhật dự án này | Project |
| 403009 | FORBIDDEN | `PROJECT_DELETE_DENIED` | Chỉ OWNER mới được xóa dự án | Project |
| 404005 | NOT_FOUND | `PROJECT_NOT_FOUND` | Không tìm thấy dự án | Project |
| 409006 | CONFLICT | `PROJECT_CODE_ALREADY_EXISTS` | Mã dự án đã tồn tại | Project |
| 409202 | CONFLICT | `PROJECT_NOT_EDITABLE` | Dự án hiện tại không cho phép chỉnh sửa | Project |
| 409203 | CONFLICT | `PROJECT_STATUS_TRANSITION_INVALID` | Chuyển trạng thái dự án không hợp lệ | Project |
| 403004 | FORBIDDEN | `PROJECT_MEMBER_ROLE_NOT_ALLOWED` | Vai trò dự án không phù hợp với vai trò hệ thống | Project Member |
| 403007 | FORBIDDEN | `PROJECT_MEMBER_MANAGE_DENIED` | Bạn không có quyền quản lý thành viên của dự án | Project Member |
| 404006 | NOT_FOUND | `PROJECT_MEMBER_NOT_FOUND` | Không tìm thấy thành viên dự án | Project Member |
| 409007 | CONFLICT | `PROJECT_MEMBER_ALREADY_EXISTS` | Người dùng đã là thành viên của dự án | Project Member |
| 409008 | CONFLICT | `PROJECT_LAST_OWNER_CANNOT_BE_REMOVED` | Không thể xóa hoặc thay đổi vai trò của OWNER cuối cùng | Project Member |
| 409009 | CONFLICT | `PROJECT_MEMBER_CANNOT_REMOVE_SELF` | Bạn không thể tự xóa mình khỏi dự án | Project Member |
| 400403 | BAD_REQUEST | `BACKLOG_ITEM_TITLE_REQUIRED` | Tiêu đề Backlog Item không được để trống | Backlog Item |
| 403401 | FORBIDDEN | `BACKLOG_ACCESS_DENIED` | Bạn không có quyền quản lý Product Backlog | Backlog Item |
| 404401 | NOT_FOUND | `BACKLOG_ITEM_NOT_FOUND` | Không tìm thấy Backlog Item | Backlog Item |
| 409401 | CONFLICT | `BACKLOG_ITEM_NOT_EDITABLE` | Backlog Item hiện tại không cho phép chỉnh sửa | Backlog Item |
| 409402 | CONFLICT | `BACKLOG_ITEM_STATUS_TRANSITION_INVALID` | Chuyển trạng thái Backlog Item không hợp lệ | Backlog Item |
| 409403 | CONFLICT | `BACKLOG_ITEM_ALREADY_IN_SPRINT` | Backlog Item đang nằm trong Sprint | Backlog Item |
| 409404 | CONFLICT | `BACKLOG_ITEM_NOT_READY` | Backlog Item phải ở trạng thái READY trước khi đưa vào Sprint | Backlog Item |
| 409405 | CONFLICT | `BACKLOG_ITEM_NOT_IN_SPRINT` | Backlog Item không thuộc Sprint này | Backlog Item |
| 409406 | CONFLICT | `BACKLOG_ITEM_SPRINT_MISMATCH` | Backlog Item và Sprint không thuộc cùng một Project | Backlog Item |
| 409407 | CONFLICT | `BACKLOG_ITEM_DONE_INVALID` | Backlog Item chỉ được hoàn thành khi Sprint đang ACTIVE | Backlog Item |
| 400501 | BAD_REQUEST | `SPRINT_DATE_INVALID` | Ngày kết thúc Sprint không được trước ngày bắt đầu | Sprint |
| 400551 | BAD_REQUEST | `SPRINT_NOT_ACTIVE` | Sprint không ở trạng thái đang thực hiện | Sprint |
| 400552 | BAD_REQUEST | `SPRINT_EMPTY_CANNOT_START` | Không thể bắt đầu Sprint chưa có Backlog Item | Sprint |
| 400553 | BAD_REQUEST | `SPRINT_CANNOT_CANCEL` | Chỉ có thể hủy Sprint đang lập kế hoạch hoặc đang thực hiện | Sprint |
| 400555 | BAD_REQUEST | `SPRINT_CANNOT_UPDATE_CLOSED` | Không thể cập nhật Sprint đã hoàn thành hoặc đã hủy | Sprint |
| 400556 | BAD_REQUEST | `SPRINT_CANNOT_START` | Chỉ Sprint ở trạng thái PLANNING mới có thể bắt đầu | Sprint |
| 400557 | BAD_REQUEST | `SPRINT_CANNOT_COMPLETE` | Chỉ Sprint đang ACTIVE mới có thể hoàn thành | Sprint |
| 400558 | BAD_REQUEST | `SPRINT_NAME_REQUIRED` | Tên Sprint không được để trống | Sprint |
| 403501 | FORBIDDEN | `SPRINT_ACCESS_DENIED` | Bạn không có quyền quản lý Sprint | Sprint |
| 403502 | FORBIDDEN | `SPRINT_BACKLOG_ACCESS_DENIED` | Bạn không có quyền quản lý Backlog của Sprint | Sprint |
| 404501 | NOT_FOUND | `SPRINT_NOT_FOUND` | Không tìm thấy Sprint | Sprint |
| 409501 | CONFLICT | `SPRINT_NAME_ALREADY_EXISTS` | Tên Sprint đã tồn tại trong dự án | Sprint |
| 409502 | CONFLICT | `SPRINT_NOT_EDITABLE` | Sprint hiện tại không cho phép chỉnh sửa | Sprint |
| 409503 | CONFLICT | `SPRINT_NOT_EMPTY` | Sprint đang chứa Backlog Item và không thể xóa | Sprint |
| 409504 | CONFLICT | `SPRINT_NOT_PLANNING` | Chỉ Sprint ở trạng thái PLANNING mới được thay đổi Backlog | Sprint |
| 409505 | CONFLICT | `SPRINT_ALREADY_ACTIVE` | Dự án đang có một Sprint hoạt động | Sprint |
| 409506 | CONFLICT | `SPRINT_EMPTY` | Sprint phải có ít nhất một Backlog Item trước khi bắt đầu | Sprint |
| 409509 | CONFLICT | `SPRINT_HAS_UNFINISHED_ITEMS` | Sprint vẫn còn Backlog Item chưa hoàn thành | Sprint |
| 409510 | CONFLICT | `SPRINT_CANCEL_INVALID` | Sprint hiện tại không thể hủy | Sprint |
| 400601 | BAD_REQUEST | `TASK_DATE_INVALID` | Ngày kết thúc Task không được trước ngày bắt đầu | Task / Kanban |
| 400604 | BAD_REQUEST | `TASK_TITLE_REQUIRED` | Tiêu đề Task không được để trống | Task / Kanban |
| 400804 | BAD_REQUEST | `TASK_NOT_BLOCKED` | Task hiện không bị block | Task / Kanban |
| 400805 | BAD_REQUEST | `TASK_UNBLOCK_TARGET_STATUS_INVALID` | Trạng thái sau khi bỏ block không hợp lệ | Task / Kanban |
| 400962 | BAD_REQUEST | `TASK_CANCELLED_CANNOT_BE_UPDATED` | Task đã bị hủy nên không thể cập nhật | Task / Kanban |
| 400963 | BAD_REQUEST | `TASK_DONE_REOPEN_TARGET_INVALID` | Trạng thái mở lại Task không hợp lệ | Task / Kanban |
| 400964 | BAD_REQUEST | `TASK_BLOCK_REASON_REQUIRED` | Cần nhập lý do khi block Task | Task / Kanban |
| 400965 | BAD_REQUEST | `TASK_KANBAN_SPRINT_INVALID` | Task không thuộc Sprint đang hoạt động nên không thể thao tác Kanban | Task / Kanban |
| 400966 | BAD_REQUEST | `TASK_STATUS_INVALID` | Trạng thái Task không hợp lệ | Task / Kanban |
| 400967 | BAD_REQUEST | `TASK_DONE_CANNOT_BE_UPDATED_EXCEPT_REOPEN` | Task đã hoàn thành chỉ có thể reopen hoặc cập nhật dữ liệu phụ trợ | Task / Kanban |
| 403601 | FORBIDDEN | `TASK_ACCESS_DENIED` | Bạn không có quyền quản lý Task | Task / Kanban |
| 403602 | FORBIDDEN | `TASK_ASSIGN_ACCESS_DENIED` | Bạn không có quyền phân công Task | Task / Kanban |
| 403603 | FORBIDDEN | `TASK_STATUS_UPDATE_DENIED` | Bạn không có quyền cập nhật trạng thái Task | Task / Kanban |
| 404601 | NOT_FOUND | `TASK_NOT_FOUND` | Không tìm thấy Task | Task / Kanban |
| 404602 | NOT_FOUND | `KANBAN_SPRINT_NOT_FOUND` | Không tìm thấy Sprint của bảng Kanban | Task / Kanban |
| 409601 | CONFLICT | `TASK_ASSIGNEE_NOT_PROJECT_MEMBER` | Người được phân công không thuộc Project | Task / Kanban |
| 409602 | CONFLICT | `TASK_ASSIGNEE_DISABLED` | Tài khoản được phân công đã bị vô hiệu hóa | Task / Kanban |
| 409605 | CONFLICT | `TASK_ALREADY_UNASSIGNED` | Task hiện chưa được phân công | Task / Kanban |
| 409606 | CONFLICT | `TASK_STATUS_TRANSITION_INVALID` | Chuyển trạng thái Task không hợp lệ | Task / Kanban |
| 409607 | CONFLICT | `TASK_NOT_IN_ACTIVE_SPRINT` | Task phải thuộc Sprint đang hoạt động | Task / Kanban |
| 409609 | CONFLICT | `TASK_CONCURRENT_MODIFICATION` | Task đã được cập nhật bởi người khác, vui lòng tải lại dữ liệu | Task / Kanban |
| 409610 | CONFLICT | `KANBAN_POSITION_CONFLICT` | Vị trí Kanban đang bị thay đổi đồng thời, vui lòng thử lại | Task / Kanban |
| 400801 | BAD_REQUEST | `TASK_DEPENDENCY_SELF_NOT_ALLOWED` | Task không được phụ thuộc chính nó | Task Dependency |
| 400802 | BAD_REQUEST | `TASK_DEPENDENCY_CROSS_PROJECT_NOT_ALLOWED` | Không thể tạo dependency với Task thuộc dự án khác | Task Dependency |
| 400803 | BAD_REQUEST | `TASK_DEPENDENCY_CYCLE_DETECTED` | Không thể tạo dependency vì sẽ phát sinh vòng lặp | Task Dependency |
| 404801 | NOT_FOUND | `TASK_DEPENDENCY_NOT_FOUND` | Không tìm thấy dependency của Task | Task Dependency |
| 409801 | CONFLICT | `TASK_DEPENDENCY_ALREADY_EXISTS` | Dependency này đã tồn tại | Task Dependency |
| 400701 | BAD_REQUEST | `TASK_COMMENT_CONTENT_INVALID` | Nội dung bình luận không hợp lệ | Task Comment |
| 403711 | FORBIDDEN | `TASK_COMMENT_ACCESS_DENIED` | Bạn không có quyền thực hiện thao tác với bình luận này | Task Comment |
| 404702 | NOT_FOUND | `TASK_COMMENT_PARENT_NOT_FOUND` | Không tìm thấy bình luận cha | Task Comment |
| 404711 | NOT_FOUND | `TASK_COMMENT_NOT_FOUND` | Không tìm thấy bình luận Task | Task Comment |
| 409701 | CONFLICT | `TASK_COMMENT_PARENT_MISMATCH` | Bình luận cha không thuộc Task hiện tại | Task Comment |
| 409702 | CONFLICT | `TASK_COMMENT_REPLY_DEPTH_INVALID` | Hệ thống chỉ hỗ trợ reply một cấp | Task Comment |
| 400806 | BAD_REQUEST | `TASK_TIME_LOG_MINUTES_TOO_LARGE` | Một bản ghi thời gian không được vượt quá 720 phút | Task Time Log |
| 400811 | BAD_REQUEST | `TASK_TIME_LOG_MINUTES_INVALID` | Số phút làm việc phải lớn hơn 0 | Task Time Log |
| 400812 | BAD_REQUEST | `TASK_TIME_LOG_DATE_INVALID` | Ngày làm việc không hợp lệ | Task Time Log |
| 400813 | BAD_REQUEST | `TASK_TIME_LOG_FUTURE_DATE_INVALID` | Không thể ghi thời gian cho ngày trong tương lai | Task Time Log |
| 400814 | BAD_REQUEST | `TASK_TIME_LOG_DESCRIPTION_TOO_LONG` | Mô tả time log không được vượt quá 2000 ký tự | Task Time Log |
| 403801 | FORBIDDEN | `TASK_TIME_LOG_ACCESS_DENIED` | Bạn không có quyền thao tác với bản ghi thời gian này | Task Time Log |
| 403802 | FORBIDDEN | `TASK_TIME_LOG_CREATE_DENIED` | Bạn không có quyền ghi thời gian cho Task này | Task Time Log |
| 404811 | NOT_FOUND | `TASK_TIME_LOG_NOT_FOUND` | Không tìm thấy bản ghi thời gian | Task Time Log |
| 409802 | CONFLICT | `TASK_TIME_LOG_DAILY_LIMIT_EXCEEDED` | Tổng thời gian trong ngày không được vượt quá 12 giờ | Task Time Log |
| 409811 | CONFLICT | `TASK_TIME_LOG_TASK_CANCELLED` | Không thể ghi thời gian cho Task đã bị hủy | Task Time Log |
| 400610 | BAD_REQUEST | `TASK_IMPORT_FILE_REQUIRED` | Vui lòng chọn file Excel | Task Import / Excel |
| 400611 | BAD_REQUEST | `TASK_IMPORT_FILE_TYPE_INVALID` | Chỉ hỗ trợ file Excel định dạng .xlsx | Task Import / Excel |
| 400612 | BAD_REQUEST | `TASK_IMPORT_FILE_TOO_LARGE` | File Excel không được vượt quá 5 MB | Task Import / Excel |
| 400613 | BAD_REQUEST | `TASK_IMPORT_SHEET_NOT_FOUND` | Không tìm thấy sheet TASK_IMPORT | Task Import / Excel |
| 400614 | BAD_REQUEST | `TASK_IMPORT_HEADER_INVALID` | Cấu trúc cột trong file Excel không hợp lệ | Task Import / Excel |
| 400615 | BAD_REQUEST | `TASK_IMPORT_TOO_MANY_ROWS` | File Excel không được vượt quá 1000 dòng dữ liệu | Task Import / Excel |
| 400616 | BAD_REQUEST | `TASK_IMPORT_NO_DATA` | File Excel không có Task để import | Task Import / Excel |
| 400917 | BAD_REQUEST | `TASK_IMPORT_DATE_RANGE_INVALID` | Khoảng thời gian tìm kiếm import không hợp lệ | Task Import / Excel |
| 404901 | NOT_FOUND | `TASK_IMPORT_BATCH_NOT_FOUND` | Không tìm thấy batch import Task | Task Import / Excel |
| 409608 | CONFLICT | `TASK_EXCEL_TEMPLATE_NO_BACKLOG_ITEMS` | Sprint chưa có Backlog Item để tạo file Excel mẫu | Task Import / Excel |
| 500601 | INTERNAL_SERVER_ERROR | `TASK_EXCEL_TEMPLATE_GENERATION_FAILED` | Không thể tạo file Excel mẫu nhập Task | Task Import / Excel |
| 500602 | INTERNAL_SERVER_ERROR | `TASK_IMPORT_PROCESSING_FAILED` | Không thể xử lý file Excel nhập Task | Task Import / Excel |
| 400980 | BAD_REQUEST | `BUG_EVIDENCE_TITLE_REQUIRED` | Tiêu đề bằng chứng Bug không được để trống | Bug / QA |
| 400981 | BAD_REQUEST | `BUG_ASSIGNEE_NOT_PROJECT_MEMBER` | Người được gán Bug phải là thành viên Project | Bug / QA |
| 400982 | BAD_REQUEST | `BUG_TASK_NOT_IN_PROJECT` | Task liên kết không thuộc Project | Bug / QA |
| 400983 | BAD_REQUEST | `BUG_BACKLOG_ITEM_NOT_IN_PROJECT` | Backlog Item liên kết không thuộc Project | Bug / QA |
| 400984 | BAD_REQUEST | `BUG_STATUS_TRANSITION_INVALID` | Không thể chuyển trạng thái Bug theo luồng hiện tại | Bug / QA |
| 400989 | BAD_REQUEST | `BUG_REPORT_DATE_RANGE_INVALID` | Khoảng thời gian báo cáo Bug không hợp lệ | Bug / QA |
| 400990 | BAD_REQUEST | `BUG_REPORT_DATE_RANGE_TOO_LARGE` | Khoảng thời gian báo cáo Bug quá lớn | Bug / QA |
| 400991 | BAD_REQUEST | `BUG_SPRINT_NOT_IN_PROJECT` | Sprint không thuộc Project hiện tại | Bug / QA |
| 400992 | BAD_REQUEST | `BUG_STATUS_INVALID` | Trạng thái Bug không hợp lệ | Bug / QA |
| 400993 | BAD_REQUEST | `BUG_CLOSED_CANNOT_BE_UPDATED` | Không thể cập nhật Bug đã đóng hoặc đã hủy | Bug / QA |
| 400998 | BAD_REQUEST | `BUG_TITLE_REQUIRED` | Tiêu đề Bug không được để trống | Bug / QA |
| 400999 | BAD_REQUEST | `BUG_COMMENT_CONTENT_INVALID` | Nội dung bình luận Bug không được để trống | Bug / QA |
| 403903 | FORBIDDEN | `BUG_ACCESS_DENIED` | Bạn không có quyền thao tác Bug này | Bug / QA |
| 403985 | FORBIDDEN | `BUG_COMMENT_ACCESS_DENIED` | Bạn không có quyền thao tác bình luận Bug này | Bug / QA |
| 403986 | FORBIDDEN | `BUG_EVIDENCE_ACCESS_DENIED` | Bạn không có quyền thao tác bằng chứng Bug này | Bug / QA |
| 403987 | FORBIDDEN | `BUG_ATTACHMENT_ACCESS_DENIED` | Bạn không có quyền thao tác tệp đính kèm Bug này | Bug / QA |
| 404903 | NOT_FOUND | `BUG_NOT_FOUND` | Không tìm thấy Bug | Bug / QA |
| 404985 | NOT_FOUND | `BUG_COMMENT_NOT_FOUND` | Không tìm thấy bình luận Bug | Bug / QA |
| 404986 | NOT_FOUND | `BUG_EVIDENCE_NOT_FOUND` | Không tìm thấy bằng chứng Bug | Bug / QA |
| 404987 | NOT_FOUND | `BUG_ATTACHMENT_NOT_FOUND` | Không tìm thấy tệp đính kèm Bug | Bug / QA |
| 409985 | CONFLICT | `BUG_COMMENT_REPLY_DEPTH_INVALID` | Hệ thống chỉ hỗ trợ trả lời bình luận Bug một cấp | Bug / QA |
| 500981 | INTERNAL_SERVER_ERROR | `BUG_EXPORT_FAILED` | Xuất báo cáo Bug thất bại | Bug / QA |
| 404701 | NOT_FOUND | `NOTIFICATION_NOT_FOUND` | Không tìm thấy thông báo | Notification |
| 400901 | BAD_REQUEST | `DASHBOARD_TASK_FILTER_INVALID` | Không thể lọc đồng thời Task quá hạn và Task sắp đến hạn | Report / Dashboard |
| 400951 | BAD_REQUEST | `REPORT_DATE_RANGE_INVALID` | Khoảng thời gian báo cáo không hợp lệ | Report / Dashboard |
| 400952 | BAD_REQUEST | `REPORT_DATE_RANGE_TOO_LARGE` | Khoảng thời gian báo cáo không được vượt quá 366 ngày | Report / Dashboard |
| 400953 | BAD_REQUEST | `REPORT_USER_NOT_PROJECT_MEMBER` | Người dùng không phải thành viên dự án | Report / Dashboard |
| 400954 | BAD_REQUEST | `REPORT_TASK_NOT_IN_PROJECT` | Task không thuộc dự án | Report / Dashboard |
| 500991 | INTERNAL_SERVER_ERROR | `REPORT_EXPORT_FAILED` | Xuất báo cáo thất bại | Report / Dashboard |
| 500992 | INTERNAL_SERVER_ERROR | `REPORT_PDF_EXPORT_FAILED` | Xuất báo cáo PDF thất bại | Report / Dashboard |
| 400971 | BAD_REQUEST | `FILE_EMPTY` | Tệp tin không được để trống | Attachment / File / Filter |
| 400972 | BAD_REQUEST | `FILE_TOO_LARGE` | Tệp tin vượt quá dung lượng cho phép | Attachment / File / Filter |
| 400973 | BAD_REQUEST | `FILE_NAME_INVALID` | Tên tệp tin không hợp lệ | Attachment / File / Filter |
| 400974 | BAD_REQUEST | `FILE_EXTENSION_NOT_ALLOWED` | Định dạng tệp tin không được hỗ trợ | Attachment / File / Filter |
| 400975 | BAD_REQUEST | `FILE_MIME_TYPE_INVALID` | MIME type của tệp tin không hợp lệ | Attachment / File / Filter |
| 400976 | BAD_REQUEST | `FILTER_DATE_RANGE_INVALID` | Khoảng thời gian lọc không hợp lệ | Attachment / File / Filter |
| 400977 | BAD_REQUEST | `FILTER_SORT_FIELD_INVALID` | Trường sắp xếp không hợp lệ | Attachment / File / Filter |
| 400978 | BAD_REQUEST | `FILTER_PAGE_SIZE_INVALID` | Kích thước trang không hợp lệ | Attachment / File / Filter |
| 400985 | BAD_REQUEST | `FILE_STORAGE_INVALID_PATH` | Đường dẫn lưu tệp tin không hợp lệ | Attachment / File / Filter |
| 400986 | BAD_REQUEST | `FILE_ENTITY_TYPE_NOT_SUPPORTED` | Loại đối tượng đính kèm không được hỗ trợ | Attachment / File / Filter |
| 400995 | BAD_REQUEST | `FILE_SIGNATURE_INVALID` | Nội dung tệp tin không khớp với định dạng khai báo | Attachment / File / Filter |
| 400996 | BAD_REQUEST | `ATTACHMENT_ENTITY_LIMIT_EXCEEDED` | Số lượng tệp đính kèm của đối tượng đã vượt giới hạn | Attachment / File / Filter |
| 400997 | BAD_REQUEST | `PROJECT_STORAGE_LIMIT_EXCEEDED` | Dung lượng lưu trữ tệp tin của dự án đã vượt giới hạn | Attachment / File / Filter |
| 403981 | FORBIDDEN | `ATTACHMENT_ACCESS_DENIED` | Bạn không có quyền thao tác tài liệu đính kèm này | Attachment / File / Filter |
| 404981 | NOT_FOUND | `FILE_NOT_FOUND` | Không tìm thấy tệp tin | Attachment / File / Filter |
| 404982 | NOT_FOUND | `ATTACHMENT_NOT_FOUND` | Không tìm thấy tài liệu đính kèm | Attachment / File / Filter |
| 500971 | INTERNAL_SERVER_ERROR | `FILE_STORAGE_FAILED` | Lưu tệp tin thất bại | Attachment / File / Filter |
| 500983 | INTERNAL_SERVER_ERROR | `FILE_CLEANUP_FAILED` | Cleanup tệp tin thất bại | Attachment / File / Filter |
| 403991 | FORBIDDEN | `ADMIN_ONLY` | Chỉ ADMIN được phép thực hiện thao tác này | Admin / Audit / Activity |
| 404902 | NOT_FOUND | `PROJECT_ACTIVITY_NOT_FOUND` | Không tìm thấy lịch sử hoạt động dự án | Admin / Audit / Activity |
| 404991 | NOT_FOUND | `SYSTEM_AUDIT_LOG_NOT_FOUND` | Không tìm thấy system audit log | Admin / Audit / Activity |
| 429001 | TOO_MANY_REQUESTS | `TOO_MANY_REQUESTS` | Bạn thao tác quá nhiều lần, vui lòng thử lại sau | Rate Limit |
| 429003 | TOO_MANY_REQUESTS | `RATE_LIMIT_EXCEEDED` | Bạn thao tác quá nhanh, vui lòng thử lại sau | Rate Limit |
| 500001 | INTERNAL_SERVER_ERROR | `SYSTEM_ERROR` | Lỗi hệ thống, vui lòng thử lại sau | Infrastructure |
| 500002 | INTERNAL_SERVER_ERROR | `DATABASE_ERROR` | Lỗi cơ sở dữ liệu | Infrastructure |
| 500003 | INTERNAL_SERVER_ERROR | `TRANSACTION_ERROR` | Lỗi xử lý giao dịch | Infrastructure |
| 500004 | INTERNAL_SERVER_ERROR | `LAZY_LOADING_ERROR` | Lỗi tải dữ liệu liên kết | Infrastructure |
| 500007 | INTERNAL_SERVER_ERROR | `JSON_PROCESSING_ERROR` | Lỗi xử lý dữ liệu JSON | Infrastructure |

## Quy tắc sử dụng

1. Service phải ưu tiên mã lỗi cụ thể theo module, không dùng lỗi chung cho quy tắc nghiệp vụ chính.
2. `INVALID_PARAMETER` chỉ dành cho lỗi bind/type ở biên HTTP khi chưa thể xác định module.
3. `ACCESS_DENIED` dùng cho lỗi authorization chung từ Spring Security; service dùng mã `*_ACCESS_DENIED` cụ thể.
4. `SYSTEM_ERROR` là fallback cuối cùng; không trả exception message, SQL hoặc stack trace cho client.
5. Mỗi mã mới phải có numeric code duy nhất, HTTP status phù hợp và message tiếng Việt rõ nghĩa.

