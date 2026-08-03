package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSecurityValidatorTest {

    private final FileSecurityValidator validator = new FileSecurityValidator();

    @Test
    void acceptsValidPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bao-cao.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file, 1024));
    }

    @Test
    void rejectsPathTraversalFileName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../bao-cao.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes()
        );

        assertError(ErrorCode.FILE_NAME_INVALID, file, 1024);
    }

    @Test
    void rejectsExecutableFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "setup.exe",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        assertError(ErrorCode.FILE_EXTENSION_NOT_ALLOWED, file, 1024);
    }

    @Test
    void rejectsMismatchedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bao-cao.pdf",
                "image/png",
                "%PDF-1.7".getBytes()
        );

        assertError(ErrorCode.FILE_MIME_TYPE_INVALID, file, 1024);
    }

    @Test
    void rejectsOversizedFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ghi-chu.txt",
                "text/plain",
                "12345".getBytes()
        );

        assertError(ErrorCode.FILE_TOO_LARGE, file, 4);
    }

    private void assertError(
            ErrorCode expected,
            MockMultipartFile file,
            long maxSize
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(file, maxSize)
        );

        assertEquals(expected, exception.getErrorCode());
    }
}
