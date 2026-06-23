package com.project.taskmanagement.dto.request.user;

import com.project.taskmanagement.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @Schema(
                description = "Tên đăng nhập",
                example = "developer01"
        )
        @NotBlank(
                message = "Tên đăng nhập không được để trống"
        )
        @Size(
                min = 3,
                max = 100,
                message = "Tên đăng nhập phải có từ 3 đến 100 ký tự"
        )
        String username,

        @Schema(
                description = "Email người dùng",
                example = "developer01@example.com"
        )
        @NotBlank(
                message = "Email không được để trống"
        )
        @Email(
                message = "Email không đúng định dạng"
        )
        @Size(
                max = 255,
                message = "Email không được vượt quá 255 ký tự"
        )
        String email,

        @Schema(
                description = "Mật khẩu",
                example = "123456"
        )
        @NotBlank(
                message = "Mật khẩu không được để trống"
        )
        @Size(
                min = 6,
                max = 100,
                message = "Mật khẩu phải có từ 6 đến 100 ký tự"
        )
        String password,

        @Schema(
                description = """
                        Vai trò của người dùng:
                        ADMIN, PROJECT_MANAGER, SCRUM_MASTER,
                        PRODUCT_OWNER, DEVELOPER, TESTER, VIEWER
                        """,
                example = "DEVELOPER"
        )
        @NotNull(
                message = "Vai trò không được để trống"
        )
        UserRole role

) {
}