package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class FileSecurityValidator {

    public static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "ps1", "sh", "jar", "dll", "js", "vbs", "msi", "scr", "com", "war"
    );

    public static final Map<String, Set<String>> ALLOWED_MIME_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed"))
    );

    public void validate(MultipartFile file, long maxFileSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String originalFileName = cleanOriginalFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        validateExtension(extension);
        validateMimeType(extension, file.getContentType());
        validateMagicBytes(extension, file);
    }

    public String cleanOriginalFileName(String originalFileName) {
        String cleaned = StringUtils.cleanPath(originalFileName == null ? "" : originalFileName);

        if (cleaned.isBlank() || cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            throw new BusinessException(ErrorCode.FILE_NAME_INVALID);
        }

        return cleaned;
    }

    public String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void validateExtension(String extension) {
        if (BLOCKED_EXTENSIONS.contains(extension) || !ALLOWED_MIME_BY_EXTENSION.containsKey(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }
    }

    private void validateMimeType(String extension, String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return;
        }

        Set<String> allowedMimeTypes = ALLOWED_MIME_BY_EXTENSION.getOrDefault(extension, Set.of());
        if (!allowedMimeTypes.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_MIME_TYPE_NOT_ALLOWED);
        }
    }

    private void validateMagicBytes(String extension, MultipartFile file) {
        if (extension.equals("txt") || extension.equals("doc") || extension.equals("xls")) {
            return;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(8);

            if (extension.equals("pdf") && !startsWith(header, "%PDF".getBytes())) {
                throw new BusinessException(ErrorCode.FILE_SIGNATURE_INVALID);
            }

            if (extension.equals("png") && !isPng(header)) {
                throw new BusinessException(ErrorCode.FILE_SIGNATURE_INVALID);
            }

            if ((extension.equals("jpg") || extension.equals("jpeg")) && !isJpeg(header)) {
                throw new BusinessException(ErrorCode.FILE_SIGNATURE_INVALID);
            }

            if ((extension.equals("zip") || extension.equals("docx") || extension.equals("xlsx")) && !isZipBased(header)) {
                throw new BusinessException(ErrorCode.FILE_SIGNATURE_INVALID);
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_SIGNATURE_INVALID);
        }
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean isPng(byte[] header) {
        return header.length >= 8
                && (header[0] & 0xff) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47;
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xff) == 0xff
                && (header[1] & 0xff) == 0xd8
                && (header[2] & 0xff) == 0xff;
    }

    private boolean isZipBased(byte[] header) {
        return header.length >= 4
                && header[0] == 0x50
                && header[1] == 0x4B;
    }
}
