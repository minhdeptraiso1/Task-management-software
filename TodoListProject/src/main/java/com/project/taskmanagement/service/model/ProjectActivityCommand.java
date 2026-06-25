package com.project.taskmanagement.service.model;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.util.UUID;

public record ProjectActivityCommand(

        UUID projectId,

        ActivityEntityType entityType,

        UUID entityId,

        ProjectActivityAction action,

        UUID performedByUserId,

        Object oldValue,

        Object newValue

) {
}