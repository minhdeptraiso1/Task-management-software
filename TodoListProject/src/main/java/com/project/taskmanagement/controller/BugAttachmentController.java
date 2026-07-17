package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.bug.BugAttachmentResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BugAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/bugs/{bugId}/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugAttachmentController {

    BugAttachmentService bugAttachmentService;

    @Operation(summary = "Tải lên tệp đính kèm Bug")
    @PostMapping
    public ApiResponseSever<BugAttachmentResponse> upload(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
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
        BugAttachmentResponse att = bugAttachmentService.getById(projectId, bugId, attachmentId);
        Resource resource = bugAttachmentService.loadFileAsResource(projectId, bugId, attachmentId);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(att.originalFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(att.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
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
