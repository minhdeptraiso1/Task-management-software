package com.project.taskmanagement.mapper;

import com.project.taskmanagement.dto.response.projectmember.ProjectMemberResponse;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMemberMapper {

    public ProjectMemberResponse toResponse(
            ProjectMember member,
            User user
    ) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProjectId(),
                member.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                member.getRole(),
                member.getJoinedAt(),
                member.getCreatedAt()
        );
    }
}