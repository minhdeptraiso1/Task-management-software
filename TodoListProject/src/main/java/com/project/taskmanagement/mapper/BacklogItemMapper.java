package com.project.taskmanagement.mapper;

import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.entity.BacklogItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BacklogItemMapper {

    BacklogItemResponse toResponse(
            BacklogItem backlogItem
    );
}