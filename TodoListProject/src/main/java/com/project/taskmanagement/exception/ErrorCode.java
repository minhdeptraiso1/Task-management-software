package com.project.taskmanagement.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ============================================================
    // 400xxx - BAD REQUEST
    // ============================================================
    BAD_REQUEST(
            400001,
            HttpStatus.BAD_REQUEST,
            "Yêu cầu không hợp lệ"
    ),

    INVALID_REQUEST_BODY(
            400002,
            HttpStatus.BAD_REQUEST,
            "Dữ liệu gửi lên không hợp lệ"
    ),

    VALIDATION_ERROR(
            400003,
            HttpStatus.BAD_REQUEST,
            "Dữ liệu không đúng định dạng"
    ),

    MISSING_REQUIRED_FIELD(
            400004,
            HttpStatus.BAD_REQUEST,
            "Thiếu thông tin bắt buộc"
    ),

    INVALID_PARAMETER(
            400005,
            HttpStatus.BAD_REQUEST,
            "Tham số không hợp lệ"
    ),

    INVALID_ENUM_VALUE(
            400006,
            HttpStatus.BAD_REQUEST,
            "Giá trị enum không hợp lệ"
    ),

    INVALID_DATE_FORMAT(
            400007,
            HttpStatus.BAD_REQUEST,
            "Định dạng ngày không hợp lệ"
    ),

    INVALID_UUID_FORMAT(
            400008,
            HttpStatus.BAD_REQUEST,
            "Định dạng UUID không hợp lệ"
    ),
    USER_CANNOT_DISABLE_SELF(
            400110,
            HttpStatus.BAD_REQUEST,
            "Không thể disable chính tài khoản đang đăng nhập"
    ),

    USER_CANNOT_DISABLE_LAST_ADMIN(
            400111,
            HttpStatus.BAD_REQUEST,
            "Không thể disable ADMIN cuối cùng của hệ thống"
    ),

    USER_CANNOT_DEMOTE_LAST_ADMIN(
            400112,
            HttpStatus.BAD_REQUEST,
            "Không thể hạ quyền ADMIN cuối cùng của hệ thống"
    ),

    CURRENT_PASSWORD_INVALID(
            400009,
            HttpStatus.BAD_REQUEST,
            "Mật khẩu hiện tại không chính xác"
    ),

    PASSWORD_CONFIRM_NOT_MATCH(
            400010,
            HttpStatus.BAD_REQUEST,
            "Mật khẩu xác nhận không khớp"
    ),

    NEW_PASSWORD_SAME_AS_CURRENT(
            400011,
            HttpStatus.BAD_REQUEST,
            "Mật khẩu mới không được trùng với mật khẩu hiện tại"
    ),
    PROJECT_DATE_INVALID(
            400012,
            HttpStatus.BAD_REQUEST,
            "Ngày kết thúc không được trước ngày bắt đầu"
    ),
    BACKLOG_ITEM_STORY_POINTS_INVALID(
            400401,
            HttpStatus.BAD_REQUEST,
            "Story Point phải lớn hơn hoặc bằng 0"
    ),
    SPRINT_DATE_INVALID(
            400501,
            HttpStatus.BAD_REQUEST,
            "Ngày kết thúc Sprint không được trước ngày bắt đầu"
    ),
    SPRINT_NOT_ACTIVE(
            400551,
            HttpStatus.BAD_REQUEST,
            "Sprint không ở trạng thái đang thực hiện"
    ),

    SPRINT_EMPTY_CANNOT_START(
            400552,
            HttpStatus.BAD_REQUEST,
            "Không thể bắt đầu Sprint chưa có Backlog Item"
    ),

    SPRINT_CANNOT_CANCEL(
            400553,
            HttpStatus.BAD_REQUEST,
            "Chỉ có thể hủy Sprint đang lập kế hoạch hoặc đang thực hiện"
    ),

    SPRINT_STATUS_INVALID(
            400554,
            HttpStatus.BAD_REQUEST,
            "Trạng thái Sprint không hợp lệ"
    ),

    SPRINT_CANNOT_UPDATE_CLOSED(
            400555,
            HttpStatus.BAD_REQUEST,
            "Không thể cập nhật Sprint đã hoàn thành hoặc đã hủy"
    ),

    SPRINT_CANNOT_START(
            400556,
            HttpStatus.BAD_REQUEST,
            "Chỉ Sprint ở trạng thái PLANNING mới có thể bắt đầu"
    ),

    SPRINT_CANNOT_COMPLETE(
            400557,
            HttpStatus.BAD_REQUEST,
            "Chỉ Sprint đang ACTIVE mới có thể hoàn thành"
    ),

    BACKLOG_POSITION_INVALID(
            400402,
            HttpStatus.BAD_REQUEST,
            "Vị trí Backlog Item không hợp lệ"
    ),
    SPRINT_BACKLOG_ACCESS_DENIED(
            403502,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền quản lý Backlog của Sprint"
    ),
    TASK_DATE_INVALID(
            400601,
            HttpStatus.BAD_REQUEST,
            "Ngày kết thúc Task không được trước ngày bắt đầu"
    ),

    TASK_ESTIMATED_MINUTES_INVALID(
            400602,
            HttpStatus.BAD_REQUEST,
            "Thời gian dự kiến phải lớn hơn hoặc bằng 0"
    ),
    TASK_CANCELLED_CANNOT_BE_UPDATED(
            400962,
            HttpStatus.BAD_REQUEST,
            "Task đã bị hủy nên không thể cập nhật"
    ),

    TASK_STATUS_INVALID(
            400966,
            HttpStatus.BAD_REQUEST,
            "Trạng thái Task không hợp lệ"
    ),
    TASK_DONE_CANNOT_BE_UPDATED_EXCEPT_REOPEN(
            400967,
            HttpStatus.BAD_REQUEST,
            "Task đã hoàn thành chỉ có thể reopen hoặc cập nhật dữ liệu phụ trợ"
    ),

    TASK_DONE_REOPEN_TARGET_INVALID(
            400963,
            HttpStatus.BAD_REQUEST,
            "Trạng thái mở lại Task không hợp lệ"
    ),

    TASK_BLOCK_REASON_REQUIRED(
            400964,
            HttpStatus.BAD_REQUEST,
            "Cần nhập lý do khi block Task"
    ),

    TASK_KANBAN_SPRINT_INVALID(
            400965,
            HttpStatus.BAD_REQUEST,
            "Task không thuộc Sprint đang hoạt động nên không thể thao tác Kanban"
    ),

    TASK_DEPENDENCY_SELF_NOT_ALLOWED(
            400801,
            HttpStatus.BAD_REQUEST,
            "Task không được phụ thuộc chính nó"
    ),

    TASK_DEPENDENCY_CROSS_PROJECT_NOT_ALLOWED(
            400802,
            HttpStatus.BAD_REQUEST,
            "Không thể tạo dependency với Task thuộc dự án khác"
    ),

    TASK_DEPENDENCY_CYCLE_DETECTED(
            400803,
            HttpStatus.BAD_REQUEST,
            "Không thể tạo dependency vì sẽ phát sinh vòng lặp"
    ),

    TASK_NOT_BLOCKED(
            400804,
            HttpStatus.BAD_REQUEST,
            "Task hiện không bị block"
    ),

    TASK_UNBLOCK_TARGET_STATUS_INVALID(
            400805,
            HttpStatus.BAD_REQUEST,
            "Trạng thái sau khi bỏ block không hợp lệ"
    ),

    REPORT_DATE_RANGE_INVALID(
            400951,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian báo cáo không hợp lệ"
    ),

    REPORT_DATE_RANGE_TOO_LARGE(
            400952,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian báo cáo không được vượt quá 366 ngày"
    ),

    REPORT_USER_NOT_PROJECT_MEMBER(
            400953,
            HttpStatus.BAD_REQUEST,
            "Người dùng không phải thành viên dự án"
    ),

    REPORT_TASK_NOT_IN_PROJECT(
            400954,
            HttpStatus.BAD_REQUEST,
            "Task không thuộc dự án"
    ),
    // ============================================================
    // 401xxx - UNAUTHORIZED
    // ============================================================
    INVALID_CREDENTIALS(
            401001,
            HttpStatus.UNAUTHORIZED,
            "Tên đăng nhập hoặc mật khẩu không đúng"
    ),

    TOKEN_EXPIRED(
            401002,
            HttpStatus.UNAUTHORIZED,
            "Phiên đăng nhập đã hết hạn"
    ),

    TOKEN_REVOKED(
            401003,
            HttpStatus.UNAUTHORIZED,
            "Token đã bị thu hồi"
    ),

    INVALID_TOKEN(
            401004,
            HttpStatus.UNAUTHORIZED,
            "Token không hợp lệ"
    ),

    UNAUTHENTICATED(
            401005,
            HttpStatus.UNAUTHORIZED,
            "Bạn chưa đăng nhập"
    ),

    REFRESH_TOKEN_EXPIRED(
            401006,
            HttpStatus.UNAUTHORIZED,
            "Refresh token đã hết hạn"
    ),

    REFRESH_TOKEN_INVALID(
            401007,
            HttpStatus.UNAUTHORIZED,
            "Refresh token không hợp lệ"
    ),
    INVALID_ACCESS_TOKEN(
            401008,
            HttpStatus.UNAUTHORIZED,
            "Access token không hợp lệ"
    ),

    // ============================================================
    // 403xxx - FORBIDDEN
    // ============================================================
    ACCESS_DENIED(
            403001,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thực hiện chức năng này"
    ),

    ACCOUNT_DISABLED(
            403002,
            HttpStatus.FORBIDDEN,
            "Tài khoản đã bị khóa"
    ),

    ACCOUNT_NOT_ACTIVE(
            403003,
            HttpStatus.FORBIDDEN,
            "Tài khoản chưa được kích hoạt"
    ),
    PROJECT_MEMBER_ROLE_NOT_ALLOWED(
            403004,
            HttpStatus.FORBIDDEN,
            "Vai trò dự án không phù hợp với vai trò hệ thống"
    ),
    PROJECT_CREATE_FORBIDDEN(
            403005,
            HttpStatus.FORBIDDEN,
            "Chỉ tài khoản quản lý mới được tạo dự án"
    ),
    PROJECT_ACCESS_DENIED(
            403006,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền truy cập dự án này"
    ),
    PROJECT_MEMBER_MANAGE_DENIED(
            403007,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền quản lý thành viên của dự án"
    ),
    PROJECT_UPDATE_DENIED(
            403008,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền cập nhật dự án này"
    ),

    PROJECT_DELETE_DENIED(
            403009,
            HttpStatus.FORBIDDEN,
            "Chỉ OWNER mới được xóa dự án"
    ),
    NOTIFICATION_ACCESS_DENIED(
            403701,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền truy cập thông báo này"
    ),
    BACKLOG_ACCESS_DENIED(
            403401,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền quản lý Product Backlog"
    ),
    SPRINT_ACCESS_DENIED(
            403501,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền quản lý Sprint"
    ),
    TASK_ACCESS_DENIED(
            403601,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền quản lý Task"
    ),

    TASK_ASSIGN_ACCESS_DENIED(
            403602,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền phân công Task"
    ),
    ADMIN_ONLY(
            403991,
            HttpStatus.FORBIDDEN,
            "Chỉ ADMIN được phép thực hiện thao tác này"
    ),

    SCHEDULER_RUN_FORBIDDEN(
            403995,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền chạy scheduler thủ công"
    ),
    // ============================================================
    // 404xxx - NOT FOUND
    // ============================================================
    USER_NOT_FOUND(
            404001,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy người dùng"
    ),

    RESOURCE_NOT_FOUND(
            404002,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy dữ liệu"
    ),

    TOKEN_SESSION_NOT_FOUND(
            404003,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy phiên đăng nhập"
    ),

    API_NOT_FOUND(
            404004,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy API yêu cầu"
    ),
    PROJECT_NOT_FOUND(
            404005,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy dự án"
    ),
    PROJECT_MEMBER_NOT_FOUND(
            404006,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thành viên dự án"
    ),
    NOTIFICATION_NOT_FOUND(
            404701,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thông báo"
    ),
    BACKLOG_ITEM_NOT_FOUND(
            404401,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy Backlog Item"
    ),
    SPRINT_NOT_FOUND(
            404501,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy Sprint"
    ),
    TASK_NOT_FOUND(
            404601,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy Task"
    ),
    TASK_DEPENDENCY_NOT_FOUND(
            404801,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy dependency của Task"
    ),
    PROJECT_ACTIVITY_NOT_FOUND(
            404902,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy lịch sử hoạt động dự án"
    ),
    // ============================================================
    // 405xxx - METHOD NOT ALLOWED
    // ============================================================
    SYSTEM_AUDIT_LOG_NOT_FOUND(
            404991,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy system audit log"
    ),

    METHOD_NOT_ALLOWED(
            405001,
            HttpStatus.METHOD_NOT_ALLOWED,
            "Phương thức HTTP không được hỗ trợ"
    ),

    // ============================================================
    // 409xxx - CONFLICT
    // ============================================================
    USERNAME_ALREADY_EXISTS(
            409001,
            HttpStatus.CONFLICT,
            "Tên đăng nhập đã tồn tại"
    ),

    EMAIL_ALREADY_EXISTS(
            409002,
            HttpStatus.CONFLICT,
            "Email đã tồn tại"
    ),

    DATA_ALREADY_EXISTS(
            409003,
            HttpStatus.CONFLICT,
            "Dữ liệu đã tồn tại"
    ),

    DATA_INTEGRITY_VIOLATION(
            409004,
            HttpStatus.CONFLICT,
            "Dữ liệu bị trùng hoặc vi phạm ràng buộc"
    ),

    FOREIGN_KEY_VIOLATION(
            409005,
            HttpStatus.CONFLICT,
            "Dữ liệu liên kết không tồn tại hoặc không hợp lệ"
    ),
    PROJECT_CODE_ALREADY_EXISTS(
            409006,
            HttpStatus.CONFLICT,
            "Mã dự án đã tồn tại"
    ),
    PROJECT_MEMBER_ALREADY_EXISTS(
            409007,
            HttpStatus.CONFLICT,
            "Người dùng đã là thành viên của dự án"
    ),
    PROJECT_LAST_OWNER_CANNOT_BE_REMOVED(
            409008,
            HttpStatus.CONFLICT,
            "Không thể xóa hoặc thay đổi vai trò của OWNER cuối cùng"
    ),

    PROJECT_MEMBER_CANNOT_REMOVE_SELF(
            409009,
            HttpStatus.CONFLICT,
            "Bạn không thể tự xóa mình khỏi dự án"
    ),
    PROJECT_NOT_EDITABLE(
            409202,
            HttpStatus.CONFLICT,
            "Dự án hiện tại không cho phép chỉnh sửa"
    ),

    PROJECT_STATUS_TRANSITION_INVALID(
            409203,
            HttpStatus.CONFLICT,
            "Chuyển trạng thái dự án không hợp lệ"
    ),

    PROJECT_ALREADY_DELETED(
            409204,
            HttpStatus.CONFLICT,
            "Dự án đã bị xóa"
    ),
    BACKLOG_ITEM_NOT_EDITABLE(
            409401,
            HttpStatus.CONFLICT,
            "Backlog Item hiện tại không cho phép chỉnh sửa"
    ),

    BACKLOG_ITEM_STATUS_TRANSITION_INVALID(
            409402,
            HttpStatus.CONFLICT,
            "Chuyển trạng thái Backlog Item không hợp lệ"
    ),

    BACKLOG_ITEM_ALREADY_IN_SPRINT(
            409403,
            HttpStatus.CONFLICT,
            "Backlog Item đang nằm trong Sprint"
    ),
    SPRINT_NOT_EDITABLE(
            409502,
            HttpStatus.CONFLICT,
            "Sprint hiện tại không cho phép chỉnh sửa"
    ),

    SPRINT_NOT_EMPTY(
            409503,
            HttpStatus.CONFLICT,
            "Sprint đang chứa Backlog Item và không thể xóa"
    ),
    SPRINT_NAME_ALREADY_EXISTS(
            409501,
            HttpStatus.CONFLICT,
            "Tên Sprint đã tồn tại trong dự án"
    ),
    BACKLOG_ITEM_NOT_READY(
            409404,
            HttpStatus.CONFLICT,
            "Backlog Item phải ở trạng thái READY trước khi đưa vào Sprint"
    ),

    BACKLOG_ITEM_NOT_IN_SPRINT(
            409405,
            HttpStatus.CONFLICT,
            "Backlog Item không thuộc Sprint này"
    ),

    BACKLOG_ITEM_SPRINT_MISMATCH(
            409406,
            HttpStatus.CONFLICT,
            "Backlog Item và Sprint không thuộc cùng một Project"
    ),
    SPRINT_NOT_PLANNING(
            409504,
            HttpStatus.CONFLICT,
            "Chỉ Sprint ở trạng thái PLANNING mới được thay đổi Backlog"
    ),
    SPRINT_ALREADY_ACTIVE(
            409505,
            HttpStatus.CONFLICT,
            "Dự án đang có một Sprint hoạt động"
    ),

    SPRINT_EMPTY(
            409506,
            HttpStatus.CONFLICT,
            "Sprint phải có ít nhất một Backlog Item trước khi bắt đầu"
    ),

    SPRINT_START_INVALID(
            409507,
            HttpStatus.CONFLICT,
            "Chỉ Sprint ở trạng thái PLANNING mới được bắt đầu"
    ),

    SPRINT_COMPLETE_INVALID(
            409508,
            HttpStatus.CONFLICT,
            "Chỉ Sprint ở trạng thái ACTIVE mới được hoàn thành"
    ),

    SPRINT_HAS_UNFINISHED_ITEMS(
            409509,
            HttpStatus.CONFLICT,
            "Sprint vẫn còn Backlog Item chưa hoàn thành"
    ),

    SPRINT_CANCEL_INVALID(
            409510,
            HttpStatus.CONFLICT,
            "Sprint hiện tại không thể hủy"
    ),

    BACKLOG_ITEM_DONE_INVALID(
            409407,
            HttpStatus.CONFLICT,
            "Backlog Item chỉ được hoàn thành khi Sprint đang ACTIVE"
    ),
    TASK_ASSIGNEE_NOT_PROJECT_MEMBER(
            409601,
            HttpStatus.CONFLICT,
            "Người được phân công không thuộc Project"
    ),

    TASK_ASSIGNEE_DISABLED(
            409602,
            HttpStatus.CONFLICT,
            "Tài khoản được phân công đã bị vô hiệu hóa"
    ),

    TASK_BACKLOG_ITEM_MISMATCH(
            409603,
            HttpStatus.CONFLICT,
            "Backlog Item không thuộc Project"
    ),

    TASK_NOT_EDITABLE(
            409604,
            HttpStatus.CONFLICT,
            "Task hiện tại không cho phép chỉnh sửa"
    ),

    TASK_ALREADY_UNASSIGNED(
            409605,
            HttpStatus.CONFLICT,
            "Task hiện chưa được phân công"
    ),

    USER_ROLE_CONFLICT_WITH_PROJECT_ROLE(
            409110,
            HttpStatus.CONFLICT,
            "System role mới không phù hợp với vai trò hiện tại trong Project"
    ),

    // ============================================================
    // 415xxx - UNSUPPORTED MEDIA TYPE
    // ============================================================
    UNSUPPORTED_MEDIA_TYPE(
            415001,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Định dạng dữ liệu không được hỗ trợ"
    ),

    // ============================================================
    // 429xxx - TOO MANY REQUESTS
    // ============================================================
    TOO_MANY_REQUESTS(
            429001,
            HttpStatus.TOO_MANY_REQUESTS,
            "Bạn thao tác quá nhiều lần, vui lòng thử lại sau"
    ),

    LOGIN_TOO_MANY_ATTEMPTS(
            429002,
            HttpStatus.TOO_MANY_REQUESTS,
            "Bạn đăng nhập sai quá nhiều lần, vui lòng thử lại sau"
    ),
    // ============================================================
    // Lỗi tổng hợp sắp xếp sau
    // ============================================================
    TASK_STATUS_TRANSITION_INVALID(
            409606,
            HttpStatus.CONFLICT,
            "Chuyển trạng thái Task không hợp lệ"
    ),

    TASK_DEPENDENCY_ALREADY_EXISTS(
            409801,
            HttpStatus.CONFLICT,
            "Dependency này đã tồn tại"
    ),

    TASK_STATUS_UPDATE_DENIED(
            403603,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền cập nhật trạng thái Task"
    ),

    TASK_POSITION_INVALID(
            400603,
            HttpStatus.BAD_REQUEST,
            "Vị trí Task không hợp lệ"
    ),

    TASK_NOT_IN_ACTIVE_SPRINT(
            409607,
            HttpStatus.CONFLICT,
            "Task phải thuộc Sprint đang hoạt động"
    ),

    KANBAN_SPRINT_NOT_FOUND(
            404602,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy Sprint của bảng Kanban"
    ),
    TASK_EXCEL_TEMPLATE_GENERATION_FAILED(
            500601,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Không thể tạo file Excel mẫu nhập Task"
    ),

    TASK_EXCEL_TEMPLATE_NO_BACKLOG_ITEMS(
            409608,
            HttpStatus.CONFLICT,
            "Sprint chưa có Backlog Item để tạo file Excel mẫu"
    ),
    TASK_IMPORT_FILE_REQUIRED(
            400610,
            HttpStatus.BAD_REQUEST,
            "Vui lòng chọn file Excel"
    ),

    TASK_IMPORT_FILE_TYPE_INVALID(
            400611,
            HttpStatus.BAD_REQUEST,
            "Chỉ hỗ trợ file Excel định dạng .xlsx"
    ),

    TASK_IMPORT_FILE_TOO_LARGE(
            400612,
            HttpStatus.BAD_REQUEST,
            "File Excel không được vượt quá 5 MB"
    ),

    TASK_IMPORT_SHEET_NOT_FOUND(
            400613,
            HttpStatus.BAD_REQUEST,
            "Không tìm thấy sheet TASK_IMPORT"
    ),

    TASK_IMPORT_HEADER_INVALID(
            400614,
            HttpStatus.BAD_REQUEST,
            "Cấu trúc cột trong file Excel không hợp lệ"
    ),

    TASK_IMPORT_TOO_MANY_ROWS(
            400615,
            HttpStatus.BAD_REQUEST,
            "File Excel không được vượt quá 1000 dòng dữ liệu"
    ),

    TASK_IMPORT_NO_DATA(
            400616,
            HttpStatus.BAD_REQUEST,
            "File Excel không có Task để import"
    ),

    TASK_IMPORT_VALIDATION_FAILED(
            422601,
            HttpStatus.UNPROCESSABLE_ENTITY,
            "File Excel chứa dữ liệu không hợp lệ"
    ),

    TASK_IMPORT_PROCESSING_FAILED(
            500602,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Không thể xử lý file Excel nhập Task"
    ),
    TASK_IMPORT_BATCH_NOT_FOUND(
            404901,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy batch import Task"
    ),

    TASK_COMMENT_NOT_FOUND(
            404701,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy bình luận Task"
    ),

    TASK_COMMENT_ACCESS_DENIED(
            403701,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thực hiện thao tác với bình luận này"
    ),

    TASK_COMMENT_PARENT_NOT_FOUND(
            404702,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy bình luận cha"
    ),

    TASK_COMMENT_PARENT_MISMATCH(
            409701,
            HttpStatus.CONFLICT,
            "Bình luận cha không thuộc Task hiện tại"
    ),

    TASK_COMMENT_CONTENT_INVALID(
            400701,
            HttpStatus.BAD_REQUEST,
            "Nội dung bình luận không hợp lệ"
    ),

    TASK_COMMENT_REPLY_DEPTH_INVALID(
            409702,
            HttpStatus.CONFLICT,
            "Hệ thống chỉ hỗ trợ reply một cấp"
    ),
    TASK_TIME_LOG_NOT_FOUND(
            404801,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy bản ghi thời gian"
    ),

    TASK_TIME_LOG_ACCESS_DENIED(
            403801,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thao tác với bản ghi thời gian này"
    ),

    TASK_TIME_LOG_CREATE_DENIED(
            403802,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền ghi thời gian cho Task này"
    ),

    TASK_TIME_LOG_MINUTES_INVALID(
            400801,
            HttpStatus.BAD_REQUEST,
            "Số phút làm việc phải lớn hơn 0"
    ),

    TASK_TIME_LOG_DATE_INVALID(
            400802,
            HttpStatus.BAD_REQUEST,
            "Ngày làm việc không hợp lệ"
    ),

    TASK_TIME_LOG_TASK_CANCELLED(
            409801,
            HttpStatus.CONFLICT,
            "Không thể ghi thời gian cho Task đã bị hủy"
    ),

    TASK_TIME_LOG_FUTURE_DATE_INVALID(
            400803,
            HttpStatus.BAD_REQUEST,
            "Không thể ghi thời gian cho ngày trong tương lai"
    ),

    TASK_TIME_LOG_DAILY_LIMIT_EXCEEDED(
            409802,
            HttpStatus.CONFLICT,
            "Tổng thời gian trong ngày không được vượt quá 24 giờ"
    ),

    BUG_COMMENT_NOT_FOUND(
            404985,
            HttpStatus.NOT_FOUND,
            "Khong tim thay binh luan Bug"
    ),

    BUG_COMMENT_ACCESS_DENIED(
            403985,
            HttpStatus.FORBIDDEN,
            "Ban khong co quyen thao tac binh luan Bug nay"
    ),

    BUG_EVIDENCE_NOT_FOUND(
            404986,
            HttpStatus.NOT_FOUND,
            "Khong tim thay bang chung Bug"
    ),

    BUG_EVIDENCE_ACCESS_DENIED(
            403986,
            HttpStatus.FORBIDDEN,
            "Ban khong co quyen thao tac bang chung Bug nay"
    ),

    BUG_ATTACHMENT_NOT_FOUND(
            404987,
            HttpStatus.NOT_FOUND,
            "Khong tim thay file dinh kem Bug"
    ),

    BUG_ATTACHMENT_ACCESS_DENIED(
            403987,
            HttpStatus.FORBIDDEN,
            "Ban khong co quyen thao tac file dinh kem Bug nay"
    ),

    BUG_ATTACHMENT_INVALID(
            400987,
            HttpStatus.BAD_REQUEST,
            "File dinh kem Bug khong hop le"
    ),

    BUG_ATTACHMENT_TOO_LARGE(
            400988,
            HttpStatus.BAD_REQUEST,
            "File dinh kem Bug vuot qua dung luong cho phep"
    ),

    BUG_NOT_FOUND(
            404903,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy Bug"
    ),

    BUG_ACCESS_DENIED(
            403903,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thao tác Bug này"
    ),

    BUG_ASSIGNEE_NOT_PROJECT_MEMBER(
            400981,
            HttpStatus.BAD_REQUEST,
            "Người được gán Bug phải là thành viên Project"
    ),

    BUG_TASK_NOT_IN_PROJECT(
            400982,
            HttpStatus.BAD_REQUEST,
            "Task liên kết không thuộc Project"
    ),

    BUG_BACKLOG_ITEM_NOT_IN_PROJECT(
            400983,
            HttpStatus.BAD_REQUEST,
            "Backlog Item liên kết không thuộc Project"
    ),

    BUG_STATUS_TRANSITION_INVALID(
            400984,
            HttpStatus.BAD_REQUEST,
            "Không thể chuyển trạng thái Bug theo luồng hiện tại"
    ),

    BUG_STATUS_INVALID(
            400992,
            HttpStatus.BAD_REQUEST,
            "Trạng thái Bug không hợp lệ"
    ),

    BUG_CLOSED_CANNOT_BE_UPDATED(
            400993,
            HttpStatus.BAD_REQUEST,
            "Không thể cập nhật Bug đã đóng hoặc đã hủy"
    ),
    BUG_REPORT_DATE_RANGE_INVALID(
            400989,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian báo cáo Bug không hợp lệ"
    ),

    BUG_REPORT_DATE_RANGE_TOO_LARGE(
            400990,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian báo cáo Bug quá lớn"
    ),

    BUG_SPRINT_NOT_IN_PROJECT(
            400991,
            HttpStatus.BAD_REQUEST,
            "Sprint không thuộc Project hiện tại"
    ),

    BUG_EXPORT_FAILED(
            500981,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Xuất báo cáo Bug thất bại"
    ),

    EXCEL_EXPORT_FAILED(
            500982,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Xuất file Excel thất bại"
    ),

    REPORT_EXPORT_FAILED(
            500991,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Xuất báo cáo thất bại"
    ),

    REPORT_PDF_EXPORT_FAILED(
            500992,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Xuất báo cáo PDF thất bại"
    ),

    TASK_IMPORT_DATE_RANGE_INVALID(
            400917,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian tìm kiếm import không hợp lệ"
    ),

    DASHBOARD_TASK_FILTER_INVALID(
            400901,
            HttpStatus.BAD_REQUEST,
            "Không thể lọc đồng thời Task quá hạn và Task sắp đến hạn"
    ),

    FILE_EMPTY(
            400981,
            HttpStatus.BAD_REQUEST,
            "Tệp tin không được để trống"
    ),

    FILE_TOO_LARGE(
            400982,
            HttpStatus.BAD_REQUEST,
            "Tệp tin vượt quá dung lượng cho phép"
    ),

    FILE_NAME_INVALID(
            400983,
            HttpStatus.BAD_REQUEST,
            "Tên tệp tin không hợp lệ"
    ),

    FILE_EXTENSION_NOT_ALLOWED(
            400984,
            HttpStatus.BAD_REQUEST,
            "Định dạng tệp tin không được hỗ trợ"
    ),

    FILE_STORAGE_INVALID_PATH(
            400985,
            HttpStatus.BAD_REQUEST,
            "Đường dẫn lưu tệp tin không hợp lệ"
    ),

    FILE_ENTITY_TYPE_NOT_SUPPORTED(
            400986,
            HttpStatus.BAD_REQUEST,
            "Loại đối tượng đính kèm không được hỗ trợ"
    ),

    FILE_MIME_TYPE_NOT_ALLOWED(
            400994,
            HttpStatus.BAD_REQUEST,
            "MIME type của tệp tin không được hỗ trợ"
    ),

    FILE_SIGNATURE_INVALID(
            400995,
            HttpStatus.BAD_REQUEST,
            "Nội dung tệp tin không khớp với định dạng khai báo"
    ),

    ATTACHMENT_ENTITY_LIMIT_EXCEEDED(
            400996,
            HttpStatus.BAD_REQUEST,
            "Số lượng tệp đính kèm của đối tượng đã vượt giới hạn"
    ),

    PROJECT_STORAGE_LIMIT_EXCEEDED(
            400997,
            HttpStatus.BAD_REQUEST,
            "Dung lượng lưu trữ tệp tin của dự án đã vượt giới hạn"
    ),

    FILE_NOT_FOUND(
            404981,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy tệp tin"
    ),

    ATTACHMENT_NOT_FOUND(
            404982,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy tài liệu đính kèm"
    ),

    ATTACHMENT_ACCESS_DENIED(
            403981,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thao tác tài liệu đính kèm này"
    ),

    FILE_STORAGE_FAILED(
            500981,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lưu tệp tin thất bại"
    ),

    FILE_CLEANUP_FAILED(
            500983,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Cleanup tệp tin thất bại"
    ),

    FILTER_DATE_RANGE_INVALID(
            400991,
            HttpStatus.BAD_REQUEST,
            "Khoảng thời gian lọc không hợp lệ"
    ),

    FILTER_SORT_FIELD_INVALID(
            400992,
            HttpStatus.BAD_REQUEST,
            "Trường sắp xếp không hợp lệ"
    ),

    FILTER_PAGE_SIZE_INVALID(
            400993,
            HttpStatus.BAD_REQUEST,
            "Kích thước trang không hợp lệ"
    ),

    // ============================================================
    // 500xxx - INTERNAL SERVER ERROR
    // ============================================================
    SYSTEM_ERROR(
            500001,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi hệ thống, vui lòng thử lại sau"
    ),

    DATABASE_ERROR(
            500002,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi cơ sở dữ liệu"
    ),

    TRANSACTION_ERROR(
            500003,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý giao dịch"
    ),

    LAZY_LOADING_ERROR(
            500004,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi tải dữ liệu liên kết"
    ),

    REDIS_ERROR(
            500005,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi kết nối Redis"
    ),

    CACHE_ERROR(
            500006,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý cache"
    ),

    JSON_PROCESSING_ERROR(
            500007,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý dữ liệu JSON"
    ),

    WEBSOCKET_ERROR(
            500008,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý WebSocket"
    ),

    DAILY_DIGEST_SEND_FAILED(
            500995,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Gửi daily digest thất bại"
    );


    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
