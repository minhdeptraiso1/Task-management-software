package com.project.taskmanagement.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(
                message = "Mật khẩu hiện tại không được để trống"
        )
        String currentPassword,

        @NotBlank(
                message = "Mật khẩu mới không được để trống"
        )
        @Size(
                min = 6,
                max = 100,
                message = "Mật khẩu mới phải có từ 6 đến 100 ký tự"
        )
        String newPassword,

        @NotBlank(
                message = "Mật khẩu xác nhận không được để trống"
        )
        String confirmPassword

) {
}