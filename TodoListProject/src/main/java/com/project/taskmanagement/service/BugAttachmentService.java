package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.bug.BugAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BugAttachmentService {

    BugAttachmentResponse upload(UUID projectId, UUID bugId, MultipartFile file);

    List<BugAttachmentResponse> getAll(UUID projectId, UUID bugId);

    BugAttachmentResponse getById(UUID projectId, UUID bugId, UUID attachmentId);

    Resource loadFileAsResource(UUID projectId, UUID bugId, UUID attachmentId);

    void delete(UUID projectId, UUID bugId, UUID attachmentId);
}
