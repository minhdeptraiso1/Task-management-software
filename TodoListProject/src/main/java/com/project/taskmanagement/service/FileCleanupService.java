package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.attachment.FileCleanupResultResponse;

public interface FileCleanupService {

    FileCleanupResultResponse cleanupDeletedAttachmentFiles(int limit);

    FileCleanupResultResponse cleanupOrphanAttachmentFiles(int limit);
}
