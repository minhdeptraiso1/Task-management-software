package com.project.taskmanagement.service.model;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;

import java.util.Map;
import java.util.UUID;

public record TaskImportValidationContext(
        Map<UUID, BacklogItem> backlogItemsById,
        Map<String, User> usersByEmail,
        Map<UUID, ProjectMember> membersByUserId
) {
}
