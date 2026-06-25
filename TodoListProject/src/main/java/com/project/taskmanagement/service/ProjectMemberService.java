package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.projectmember.AddProjectMemberRequest;
import com.project.taskmanagement.dto.request.projectmember.UpdateProjectMemberRoleRequest;
import com.project.taskmanagement.dto.response.projectmember.ProjectMemberResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {

    ProjectMemberResponse addMember(
            UUID projectId,
            AddProjectMemberRequest request
    );

    List<ProjectMemberResponse> getMembers(
            UUID projectId
    );

    ProjectMemberResponse updateMemberRole(
            UUID projectId,
            UUID memberId,
            UpdateProjectMemberRoleRequest request
    );

    void removeMember(
            UUID projectId,
            UUID memberId
    );
}