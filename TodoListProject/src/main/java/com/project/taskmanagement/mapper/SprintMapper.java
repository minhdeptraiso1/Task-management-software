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
    @Mapping(
            target = "taskCount",
            source = "taskCount"
    )
    @Mapping(
            target = "completedTaskCount",
            source = "completedTaskCount"
    )
    @Mapping(
            target = "completionRate",
            source = "completionRate"
    )
    SprintResponse toResponse(
            Sprint sprint,
            long backlogItemCount,
            long taskCount,
            long completedTaskCount,
            double completionRate
    );

    default SprintResponse toResponse(
            Sprint sprint,
            long backlogItemCount
    ) {
        return toResponse(
                sprint,
                backlogItemCount,
                0L,
                0L,
                0.0
        );
    }
}
