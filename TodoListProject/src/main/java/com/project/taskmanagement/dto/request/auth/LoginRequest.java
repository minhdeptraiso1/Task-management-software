package com.project.taskmanagement.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Schema(
                description = "Email đăng nhập",
                example = "admin@example.com"
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
        String password

) {
}