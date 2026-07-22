package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.attachment.AttachmentPageResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentUsageResponse;
import com.project.taskmanagement.dto.response.attachment.FileSecuritySummaryResponse;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.service.model.LoadedFile;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AttachmentService {

    AttachmentResponse upload(UUID projectId, AttachmentEntityType entityType, UUID entityId, MultipartFile file);

    AttachmentPageResponse getAttachments(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId,
            Pageable pageable
    );

    LoadedFile download(UUID projectId, UUID attachmentId);

    void delete(UUID projectId, UUID attachmentId);

    AttachmentPageResponse getProjectAttachments(UUID projectId, Pageable pageable);

    AttachmentUsageResponse getProjectUsage(UUID projectId);

    FileSecuritySummaryResponse getFileSecuritySummary(UUID projectId);
}
