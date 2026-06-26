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
    // ============================================================
    // 405xxx - METHOD NOT ALLOWED
    // ============================================================
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