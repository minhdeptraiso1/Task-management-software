package com.project.taskmanagement.dto.request.user;

import com.project.taskmanagement.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UserSearchRequest(

        @Schema(
                description = "Từ khóa tìm theo username hoặc email",
                example = "developer"
        )
        @Size(max = 100, message = "Từ khóa tìm kiếm người dùng không được vượt quá 100 ký tự")
        String keyword,

        @Schema(
                description = """
                        Lọc theo vai trò:
                        ADMIN, PROJECT_MANAGER, SCRUM_MASTER,
                        PRODUCT_OWNER, DEVELOPER, TESTER, VIEWER
                        """,
                example = "DEVELOPER"
        )
        UserRole role,

        @Schema(
                description = "Lọc theo trạng thái hoạt động",
                example = "true"
        )
        Boolean enabled

) {
}
