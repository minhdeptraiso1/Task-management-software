package com.project.taskmanagement.mapper;

import com.project.taskmanagement.dto.response.user.UserResponse;
import com.project.taskmanagement.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

}