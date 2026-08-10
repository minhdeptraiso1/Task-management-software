package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.attachment.AttachmentPageResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentUsageResponse;
import com.project.taskmanagement.dto.response.attachment.FileSecuritySummaryResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.service.AttachmentService;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.LoadedFile;
import com.project.taskmanagement.service.validation.PageableValidator;
import com.project.taskmanagement.util.DownloadHeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.Set;

@Tag(name = OpenApiTags.ATTACHMENTS, description = "Upload, download, tra cứu và xóa mềm file đính kèm")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentController {

    private static final Set<String> ATTACHMENT_SORT_FIELDS =
            Set.of(
                    "createdAt",
                    "updatedAt",
                    "originalFileName",
                    "sizeBytes"
            );

    AttachmentService attachmentService;
    RateLimitService rateLimitService;
    CurrentUserService currentUserService;

    @Operation(
            summary = "Upload attachment cho entity",
            description = "Upload file multipart cho Task, Task Comment, Bug hoặc Bug Evidence. File được kiểm tra loại, dung lượng và quyền trong Project."
    )
    @PostMapping(path = "/{entityType}/{entityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseSever<AttachmentResponse> upload(
            @PathVariable UUID projectId,
            @PathVariable AttachmentEntityType entityType,
            @PathVariable UUID entityId,
            @RequestPart("file") MultipartFile file
    ) {
        rateLimitService.check(
                RateLimitAction.ATTACHMENT_UPLOAD,
                currentUserService
                        .getActiveCurrentUser()
                        .getId()
                        .toString()
        );

        return ApiResponseSever.ok(attachmentService.upload(projectId, entityType, entityId, file));
    }

    @Operation(summary = "Lấy danh sách tất cả attachment của Project")
    @GetMapping
    public ApiResponseSever<AttachmentPageResponse> getProjectAttachments(
            @PathVariable UUID projectId,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable
    ) {
        PageableValidator.validate(pageable, ATTACHMENT_SORT_FIELDS);

        return ApiResponseSever.ok(attachmentService.getProjectAttachments(projectId, pageable));
    }

    @Operation(summary = "Lấy danh sách attachment của entity")
    @GetMapping("/{entityType}/{entityId}")
    public ApiResponseSever<AttachmentPageResponse> getAttachments(
            @PathVariable UUID projectId,
            @PathVariable AttachmentEntityType entityType,
            @PathVariable UUID entityId,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        PageableValidator.validate(pageable, ATTACHMENT_SORT_FIELDS);

        return ApiResponseSever.ok(attachmentService.getAttachments(projectId, entityType, entityId, pageable));
    }

    @Operation(summary = "Lấy dung lượng attachment đã dùng của Project")
    @GetMapping("/usage")
    public ApiResponseSever<AttachmentUsageResponse> getProjectUsage(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(attachmentService.getProjectUsage(projectId));
    }

    @Operation(summary = "Lấy cấu hình bảo mật file attachment")
    @GetMapping("/security-summary")
    public ApiResponseSever<FileSecuritySummaryResponse> getFileSecuritySummary(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(attachmentService.getFileSecuritySummary(projectId));
    }

    @Operation(
            summary = "Download attachment",
            description = "Tải file qua API có kiểm tra quyền Project; thư mục lưu trữ không được public trực tiếp."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Nội dung file nhị phân",
            content = @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable UUID projectId,
            @PathVariable UUID attachmentId
    ) {
        LoadedFile loadedFile = attachmentService.download(projectId, attachmentId);
        MediaType mediaType = loadedFile.contentType() == null || loadedFile.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(loadedFile.contentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(loadedFile.sizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        DownloadHeaderUtils.attachmentContentDisposition(loadedFile.originalFileName())
                )
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(loadedFile.resource());
    }

    @Operation(summary = "Xóa mềm attachment")
    @DeleteMapping("/{attachmentId}")
    public ApiResponseSever<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID attachmentId
    ) {
        attachmentService.delete(projectId, attachmentId);
        return ApiResponseSever.ok();
    }
}
