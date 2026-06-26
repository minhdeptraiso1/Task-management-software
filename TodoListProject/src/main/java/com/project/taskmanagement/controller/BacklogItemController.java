package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.backlog.*;
import com.project.taskmanagement.dto.response.backlog.BacklogItemPageResponse;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BacklogItemService;
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
        "/projects/{projectId}/backlog-items"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class BacklogItemController {

    BacklogItemService backlogItemService;

    @Operation(
            summary = "Tạo Backlog Item"
    )
    @PostMapping
    public ResponseEntity<
            ApiResponseSever<BacklogItemResponse>
            > create(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            CreateBacklogItemRequest request
    ) {
        BacklogItemResponse response =
                backlogItemService.create(
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
            summary = "Tìm kiếm Product Backlog"
    )
    @GetMapping
    public ApiResponseSever<BacklogItemPageResponse>
    search(
            @PathVariable
            UUID projectId,

            @ParameterObject
            BacklogSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                backlogItemService.search(
                        projectId,
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Xem chi tiết Backlog Item"
    )
    @GetMapping("/{itemId}")
    public ApiResponseSever<BacklogItemResponse>
    getById(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId
    ) {
        return ApiResponseSever.ok(
                backlogItemService.getById(
                        projectId,
                        itemId
                )
        );
    }

    @Operation(
            summary = "Cập nhật Backlog Item"
    )
    @PatchMapping("/{itemId}")
    public ApiResponseSever<BacklogItemResponse>
    update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdateBacklogItemRequest request
    ) {
        return ApiResponseSever.ok(
                backlogItemService.update(
                        projectId,
                        itemId,
                        request
                )
        );
    }

    @Operation(
            summary = "Đổi trạng thái Backlog Item"
    )
    @PatchMapping("/{itemId}/status")
    public ApiResponseSever<BacklogItemResponse>
    updateStatus(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdateBacklogItemStatusRequest request
    ) {
        return ApiResponseSever.ok(
                backlogItemService.updateStatus(
                        projectId,
                        itemId,
                        request
                )
        );
    }

    @Operation(
            summary = "Đổi độ ưu tiên Backlog Item"
    )
    @PatchMapping("/{itemId}/priority")
    public ApiResponseSever<BacklogItemResponse>
    updatePriority(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdateBacklogPriorityRequest request
    ) {
        return ApiResponseSever.ok(
                backlogItemService.updatePriority(
                        projectId,
                        itemId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa mềm Backlog Item"
    )
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId
    ) {
        backlogItemService.delete(
                projectId,
                itemId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Thay đổi vị trí Backlog Item"
    )
    @PatchMapping("/{itemId}/position")
    public ApiResponseSever<BacklogItemResponse>
    updatePosition(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdateBacklogPositionRequest request
    ) {
        return ApiResponseSever.ok(
                backlogItemService.updatePosition(
                        projectId,
                        itemId,
                        request
                )
        );
    }
}