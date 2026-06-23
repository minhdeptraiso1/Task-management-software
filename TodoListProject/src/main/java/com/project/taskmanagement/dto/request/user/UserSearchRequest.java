package com.project.taskmanagement.dto.request.user;

import com.project.taskmanagement.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserSearchRequest(

        @Schema(
                description = "Từ khóa tìm theo username hoặc email",
                example = "developer"
        )
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