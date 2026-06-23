package com.project.taskmanagement.dto.response.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
