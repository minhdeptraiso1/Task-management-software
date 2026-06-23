package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.user.CreateUserRequest;
import com.project.taskmanagement.dto.request.user.UpdateUserRequest;
import com.project.taskmanagement.dto.request.user.UserSearchRequest;
import com.project.taskmanagement.dto.response.user.UserResponse;
import com.project.taskmanagement.dto.response.user.UserPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserPageResponse searchUsers(
            UserSearchRequest request,
            Pageable pageable
    );

    void deleteUserById(UUID userId);

    UserResponse getCurrentUser(String username);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);
}
