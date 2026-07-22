package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.FileSecurityProperties;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.service.FileStorageService;
import com.project.taskmanagement.service.model.LoadedFile;
import com.project.taskmanagement.service.model.StoredFile;
import com.project.taskmanagement.service.validation.FileSecurityValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

    FileSecurityProperties fileSecurityProperties;
    FileSecurityValidator fileSecurityValidator;

    @Override
    public StoredFile store(UUID projectId, AttachmentEntityType entityType, UUID entityId, MultipartFile file) {
        fileSecurityValidator.validate(file, fileSecurityProperties.maxFileSizeBytesOrDefault());

        String originalFileName = fileSecurityValidator.cleanOriginalFileName(file.getOriginalFilename());
        String extension = fileSecurityValidator.extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;
        Path directory = buildDirectory(projectId, entityType, entityId);

        try {
            Files.createDirectories(directory);
            Path destination = directory.resolve(storedFileName).normalize();

            if (!destination.startsWith(directory)) {
                throw new BusinessException(ErrorCode.FILE_STORAGE_INVALID_PATH);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    file.getContentType(),
                    extension,
                    file.getSize(),
                    normalizeStoragePath(destination)
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    @Override
    public LoadedFile load(String storagePath, String originalFileName, String contentType, long sizeBytes) {
        Path storage = normalizeExistingStoragePath(storagePath);
        Resource resource = new FileSystemResource(storage);

        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        return new LoadedFile(resource, originalFileName, contentType, sizeBytes);
    }

    @Override
    public void deletePhysicalFileIfExists(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(normalizeExistingStoragePath(storagePath));
        } catch (IOException ignored) {
            // Metadata da soft delete; file vat ly co the cleanup lai o lan sau.
        }
    }

    private Path buildDirectory(UUID projectId, AttachmentEntityType entityType, UUID entityId) {
        Path root = storageRoot();
        Path directory = root.resolve(projectId.toString())
                .resolve(entityType.name())
                .resolve(entityId.toString())
                .normalize();

        if (!directory.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_INVALID_PATH);
        }

        return directory;
    }

    private Path normalizeExistingStoragePath(String storagePath) {
        Path root = storageRoot();
        Path storage = Paths.get(storagePath).toAbsolutePath().normalize();

        if (!storage.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_INVALID_PATH);
        }

        return storage;
    }

    private Path storageRoot() {
        return Paths.get(fileSecurityProperties.rootPathOrDefault()).toAbsolutePath().normalize();
    }

    private String normalizeStoragePath(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }
}
