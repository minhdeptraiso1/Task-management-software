package com.project.taskmanagement.dto.request.user;

import com.project.taskmanagement.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Schema(
                description = "Mật khẩu mới. Không truyền nếu không muốn đổi",
                example = "12345678"
        )
        @Size(
                min = 6,
                max = 100,
                message = "Mật khẩu phải có từ 6 đến 100 ký tự"
        )
        String password,

        @Schema(
                description = """
                        Vai trò mới:
                        ADMIN, PROJECT_MANAGER, SCRUM_MASTER,
                        PRODUCT_OWNER, DEVELOPER, TESTER, VIEWER
                        """,
                example = "SCRUM_MASTER"
        )
        UserRole role,

        @Schema(
                description = "Trạng thái hoạt động của tài khoản",
                example = "true"
        )
        Boolean enabled

) {
}