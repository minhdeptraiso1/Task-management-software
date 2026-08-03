package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.bug.BugAttachmentResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.service.BugAttachmentService;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.util.DownloadHeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/bugs/{bugId}/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugAttachmentController {

    BugAttachmentService bugAttachmentService;
    RateLimitService rateLimitService;
    CurrentUserService currentUserService;

    @Operation(summary = "Tải lên tệp đính kèm Bug")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseSever<BugAttachmentResponse> upload(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @RequestParam("file") MultipartFile file
    ) {
        rateLimitService.check(
                RateLimitAction.ATTACHMENT_UPLOAD,
                currentUserService
                        .getActiveCurrentUser()
                        .getId()
                        .toString()
        );

        return ApiResponseSever.ok(bugAttachmentService.upload(projectId, bugId, file));
    }

    @Operation(summary = "Danh sách tệp đính kèm Bug")
    @GetMapping
    public ApiResponseSever<List<BugAttachmentResponse>> getAll(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId
    ) {
        return ApiResponseSever.ok(bugAttachmentService.getAll(projectId, bugId));
    }

    @Operation(summary = "Tải xuống tệp đính kèm Bug")
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID attachmentId
    ) {
        BugAttachmentResponse attachment = bugAttachmentService.getById(projectId, bugId, attachmentId);
        Resource resource = bugAttachmentService.loadFileAsResource(projectId, bugId, attachmentId);

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.contentType() != null && !attachment.contentType().isBlank()) {
            contentType = MediaType.parseMediaType(attachment.contentType());
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(attachment.sizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        DownloadHeaderUtils.attachmentContentDisposition(attachment.originalFileName())
                )
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }

    @Operation(summary = "Xóa tệp đính kèm Bug")
    @DeleteMapping("/{attachmentId}")
    public ApiResponseSever<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID attachmentId
    ) {
        bugAttachmentService.delete(projectId, bugId, attachmentId);
        return ApiResponseSever.ok(null);
    }
}
