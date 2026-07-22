package com.project.taskmanagement.service.attachment;

import com.project.taskmanagement.enums.AttachmentEntityType;

import java.util.UUID;

public interface AttachmentEntityResolver {

    void validateEntityExists(UUID projectId, AttachmentEntityType entityType, UUID entityId);
}
