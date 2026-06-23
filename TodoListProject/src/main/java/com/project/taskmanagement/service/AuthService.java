package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.auth.ChangePasswordRequest;
import com.project.taskmanagement.dto.request.auth.LoginRequest;
import com.project.taskmanagement.dto.response.auth.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String accessToken, String refreshToken);

    void changePassword(String username, ChangePasswordRequest request);

    void logoutAll(String accessToken);
}

