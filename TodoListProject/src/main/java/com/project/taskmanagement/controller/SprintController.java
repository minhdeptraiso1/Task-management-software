package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.sprint.CreateSprintRequest;
import com.project.taskmanagement.dto.request.sprint.SprintSearchRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintRequest;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.kanban.KanbanBoardResponse;
import com.project.taskmanagement.dto.response.sprint.SprintPageResponse;
import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import com.project.taskmanagement.dto.response.taskimport.TaskImportResponse;
import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.service.SprintService;
import com.project.taskmanagement.service.SprintWorkflowService;
import com.project.taskmanagement.service.TaskExcelImportService;
import com.project.taskmanagement.service.TaskExcelTemplateService;
import com.project.taskmanagement.service.TaskService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.GeneratedExcelFile;
import com.project.taskmanagement.util.DownloadHeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = OpenApiTags.SPRINTS, description = "Quản lý Sprint, workflow, Kanban và import Task Excel")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(
        "/projects/{projectId}/sprints"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintController {

    SprintService sprintService;
    SprintWorkflowService sprintWorkflowService;
    TaskService taskService;
    TaskExcelTemplateService taskExcelTemplateService;
    TaskExcelImportService taskExcelImportService;
    RateLimitService rateLimitService;
    CurrentUserService currentUserService;

    @Operation(
            summary = "Tạo Sprint"
    )
    @PostMapping
    public ResponseEntity<
            ApiResponseSever<SprintResponse>
            > create(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            CreateSprintRequest request
    ) {
        SprintResponse response =
                sprintService.create(
                        projectId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseSever.ok(response)
                );
    }

    @Operation(
            summary = "Tìm kiếm Sprint"
    )
    @GetMapping
    public ApiResponseSever<SprintPageResponse>
    search(
            @PathVariable
            UUID projectId,

            @ParameterObject
            SprintSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                sprintService.search(
                        projectId,
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Xem chi tiết Sprint"
    )
    @GetMapping("/{sprintId}")
    public ApiResponseSever<SprintResponse>
    getById(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintService.getById(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Cập nhật Sprint PLANNING"
    )
    @PatchMapping("/{sprintId}")
    public ApiResponseSever<SprintResponse>
    update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @Valid
            @RequestBody
            UpdateSprintRequest request
    ) {
        return ApiResponseSever.ok(
                sprintService.update(
                        projectId,
                        sprintId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa mềm Sprint PLANNING",
            description = """
                    Sprint chỉ được xóa khi còn PLANNING
                    và chưa chứa Backlog Item.
                    """
    )
    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        sprintService.delete(
                projectId,
                sprintId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Đưa Backlog Item vào Sprint"
    )
    @PostMapping(
            "/{sprintId}/backlog-items/{itemId}"
    )
    public ApiResponseSever<BacklogItemResponse>
    addBacklogItem(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @PathVariable
            UUID itemId
    ) {
        return ApiResponseSever.ok(
                sprintService.addBacklogItem(
                        projectId,
                        sprintId,
                        itemId
                )
        );
    }

    @Operation(
            summary = "Lấy Backlog Item ra khỏi Sprint"
    )
    @DeleteMapping(
            "/{sprintId}/backlog-items/{itemId}"
    )
    public ApiResponseSever<BacklogItemResponse>
    removeBacklogItem(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @PathVariable
            UUID itemId
    ) {
        return ApiResponseSever.ok(
                sprintService.removeBacklogItem(
                        projectId,
                        sprintId,
                        itemId
                )
        );
    }

    @Operation(
            summary = "Bắt đầu Sprint",
            description = """
                    Sprint phải ở trạng thái PLANNING,
                    có ít nhất một Backlog Item và Project
                    chưa có Sprint ACTIVE khác.
                    """
    )
    @PatchMapping("/{sprintId}/start")
    public ApiResponseSever<SprintResponse>
    start(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintWorkflowService.start(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Hoàn thành Sprint",
            description = """
                    Sprint phải ở trạng thái ACTIVE và
                    toàn bộ Backlog Item trong Sprint
                    phải ở trạng thái DONE.
                    """
    )
    @PatchMapping("/{sprintId}/complete")
    public ApiResponseSever<SprintResponse>
    complete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintWorkflowService.complete(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Hủy Sprint",
            description = """
                    Chỉ Sprint PLANNING hoặc ACTIVE được hủy.
                    
                    Backlog Item chưa DONE sẽ được đưa
                    trở lại Product Backlog với trạng thái READY.
                    """
    )
    @PatchMapping("/{sprintId}/cancel")
    public ApiResponseSever<SprintResponse>
    cancel(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintWorkflowService.cancel(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Lấy bảng Kanban của Sprint"
    )
    @GetMapping("/{sprintId}/kanban")
    public ApiResponseSever<KanbanBoardResponse>
    getKanbanBoard(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                taskService.getKanbanBoard(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Tải file Excel mẫu nhập Task",
            description = """
                    File Excel được sinh từ Sprint.
                    File chứa danh sách Backlog Item
                    và thành viên của Project.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File Excel mẫu",
            content = @Content(
                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping(
            path = "/{sprintId}/tasks/excel-template",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> downloadTaskExcelTemplate(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        GeneratedExcelFile generatedFile =
                taskExcelTemplateService
                        .generateTemplate(
                                projectId,
                                sprintId
                        );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        DownloadHeaderUtils.attachmentContentDisposition(
                                generatedFile.fileName()
                        )
                )
                .header(
                        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION
                )
                .contentType(
                        MediaType.parseMediaType(
                                generatedFile.contentType()
                        )
                )
                .contentLength(
                        generatedFile.content().length
                )
                .body(
                        generatedFile.content()
                );
    }

    @Operation(
            summary = "Import Task từ file Excel",
            description = """
                    Đọc file Excel mẫu được sinh từ Sprint.
                    
                    Toàn bộ file được validate trước.
                    Nếu có bất kỳ dòng lỗi nào thì không Task nào
                    được tạo.
                    """
    )
    @PostMapping(
            path = "/{sprintId}/tasks/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponseSever<TaskImportResponse>
    importTasksFromExcel(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @RequestPart("file")
            MultipartFile file
    ) {
        rateLimitService.check(
                RateLimitAction.TASK_EXCEL_IMPORT,
                currentUserService
                        .getActiveCurrentUser()
                        .getId()
                        .toString()
        );

        return ApiResponseSever.ok(
                taskExcelImportService.importTasks(
                        projectId,
                        sprintId,
                        file
                )
        );
    }

}
