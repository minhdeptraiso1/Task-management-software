package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.config.FileSecurityProperties;
import com.project.taskmanagement.dto.response.attachment.FileCleanupResultResponse;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AttachmentRepository;
import com.project.taskmanagement.repository.projection.attachment.DeletedAttachmentFileView;
import com.project.taskmanagement.service.FileCleanupService;
import com.project.taskmanagement.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCleanupServiceImpl implements FileCleanupService {

    AttachmentRepository attachmentRepository;
    FileStorageService fileStorageService;
    FileSecurityProperties fileSecurityProperties;

    @Override
    @Transactional(readOnly = true)
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ATTACHMENT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.ATTACHMENT_USAGE, allEntries = true),
            @CacheEvict(value = CacheNames.GLOBAL_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.FILE_CLEANUP_RESULT, allEntries = true)
    })
    public FileCleanupResultResponse cleanupDeletedAttachmentFiles(int limit) {
        int actualLimit = limit <= 0 ? 100 : Math.min(limit, 1000);
        Instant deletedBefore = Instant.now()
                .minus(fileSecurityProperties.keepDeletedFileDaysOrDefault(), ChronoUnit.DAYS);

        List<DeletedAttachmentFileView> files = attachmentRepository.findDeletedFilesForCleanup(
                deletedBefore,
                actualLimit
        );

        int deleted = 0;
        int failed = 0;

        for (DeletedAttachmentFileView file : files) {
            try {
                fileStorageService.deletePhysicalFileIfExists(file.getStoragePath());
                deleted++;
            } catch (Exception exception) {
                failed++;
            }
        }

        return new FileCleanupResultResponse(files.size(), deleted, failed);
    }

    @Override
    @Transactional(readOnly = true)
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ATTACHMENT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.ATTACHMENT_USAGE, allEntries = true),
            @CacheEvict(value = CacheNames.GLOBAL_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.FILE_CLEANUP_RESULT, allEntries = true)
    })
    public FileCleanupResultResponse cleanupOrphanAttachmentFiles(int limit) {
        int actualLimit = limit <= 0 ? 100 : Math.min(limit, 1000);
        Path root = Paths.get(fileSecurityProperties.rootPathOrDefault()).toAbsolutePath().normalize();

        if (!Files.exists(root)) {
            return new FileCleanupResultResponse(0, 0, 0);
        }

        int scanned = 0;
        int deleted = 0;
        int failed = 0;

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .limit(actualLimit)
                    .toList();

            for (Path file : files) {
                scanned++;
                String storagePath = file.toAbsolutePath().normalize().toString();

                if (attachmentRepository.countAllRowsByStoragePath(storagePath) > 0) {
                    continue;
                }

                try {
                    Files.deleteIfExists(file);
                    deleted++;
                } catch (IOException exception) {
                    failed++;
                }
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_CLEANUP_FAILED);
        }

        return new FileCleanupResultResponse(scanned, deleted, failed);
    }
}
