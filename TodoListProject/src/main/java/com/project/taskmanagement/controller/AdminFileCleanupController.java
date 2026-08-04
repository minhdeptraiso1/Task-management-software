package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.attachment.FileCleanupResultResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.FileCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = OpenApiTags.ADMIN, description = "Quản trị dọn dẹp file và dữ liệu đính kèm")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/files/cleanup")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFileCleanupController {

    FileCleanupService fileCleanupService;

    @Operation(summary = "Cleanup file vật lý của attachment đã xóa mềm")
    @PostMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<FileCleanupResultResponse> cleanupDeletedFiles(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponseSever.ok(fileCleanupService.cleanupDeletedAttachmentFiles(limit));
    }

    @Operation(summary = "Cleanup file vật lý không còn metadata attachment")
    @PostMapping("/orphans")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<FileCleanupResultResponse> cleanupOrphanFiles(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponseSever.ok(fileCleanupService.cleanupOrphanAttachmentFiles(limit));
    }
}
