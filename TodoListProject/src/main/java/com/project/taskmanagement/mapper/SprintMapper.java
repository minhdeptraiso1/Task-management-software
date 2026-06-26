package com.project.taskmanagement.mapper;

import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import com.project.taskmanagement.entity.Sprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(
            target = "backlogItemCount",
            source = "backlogItemCount"
    )
    SprintResponse toResponse(
            Sprint sprint,
            long backlogItemCount
    );
}