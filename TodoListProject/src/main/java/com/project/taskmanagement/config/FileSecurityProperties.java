package com.project.taskmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file-storage")
public record FileSecurityProperties(
        String rootPath,
        Long maxFileSizeBytes,
        Integer maxFilesPerEntity,
        Long maxProjectStorageBytes,
        Integer keepDeletedFileDays
) {

    public String rootPathOrDefault() {
        return rootPath == null || rootPath.isBlank() ? "uploads" : rootPath;
    }

    public long maxFileSizeBytesOrDefault() {
        return maxFileSizeBytes == null ? 10L * 1024 * 1024 : maxFileSizeBytes;
    }

    public int maxFilesPerEntityOrDefault() {
        return maxFilesPerEntity == null ? 20 : maxFilesPerEntity;
    }

    public long maxProjectStorageBytesOrDefault() {
        return maxProjectStorageBytes == null ? 500L * 1024 * 1024 : maxProjectStorageBytes;
    }

    public int keepDeletedFileDaysOrDefault() {
        return keepDeletedFileDays == null ? 30 : keepDeletedFileDays;
    }
}
