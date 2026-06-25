package com.project.taskmanagement.mapper;

import com.project.taskmanagement.dto.response.project.ProjectResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.enums.ProjectMemberRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(
            target = "currentUserRole",
            source = "currentUserRole"
    )
    ProjectResponse toResponse(
            Project project,
            ProjectMemberRole currentUserRole
    );
}