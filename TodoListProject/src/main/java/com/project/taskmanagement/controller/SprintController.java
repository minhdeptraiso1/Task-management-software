package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.sprint.CreateSprintRequest;
import com.project.taskmanagement.dto.request.sprint.SprintSearchRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintRequest;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.sprint.SprintPageResponse;
import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import com.project.taskmanagement.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
                sprintService.start(
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
                sprintService.complete(
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
                sprintService.cancel(
                        projectId,
                        sprintId
                )
        );
    }

}