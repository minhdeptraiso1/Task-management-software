package com.project.taskmanagement.service;

import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.service.model.LoadedFile;
import com.project.taskmanagement.service.model.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {

    StoredFile store(UUID projectId, AttachmentEntityType entityType, UUID entityId, MultipartFile file);

    LoadedFile load(String storagePath, String originalFileName, String contentType, long sizeBytes);

    void deletePhysicalFileIfExists(String storagePath);
}
