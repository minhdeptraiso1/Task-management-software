package com.project.taskmanagement.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token không được để trống")
        String refreshToken

) {
}