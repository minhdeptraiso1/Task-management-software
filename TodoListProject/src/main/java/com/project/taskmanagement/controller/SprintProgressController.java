package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.sprint.SprintProgressResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReminderResponse;
import com.project.taskmanagement.service.SprintProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.SPRINTS, description = "Tiến độ, snapshot và lịch sử thực hiện Sprint")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(
        "/projects/{projectId}/sprints/{sprintId}"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintProgressController {

    SprintProgressService sprintProgressService;

    // ===================== PROGRESS =====================

    @Operation(
            summary = "Lấy tiến độ Sprint"
    )
    @GetMapping("/progress")
    public ApiResponseSever<SprintProgressResponse>
    getProgress(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintProgressService
                        .getProgress(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== REMIND ENDING =====================

    @Operation(
            summary = "Gửi nhắc nhở Sprint sắp kết thúc"
    )
    @PostMapping("/notifications/remind-ending")
    public ApiResponseSever<SprintReminderResponse>
    remindEndingSoon(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintProgressService
                        .remindEndingSoon(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== REMIND OVERDUE =====================

    @Operation(
            summary = "Gửi nhắc nhở Task quá hạn trong Sprint"
    )
    @PostMapping("/notifications/remind-overdue-tasks")
    public ApiResponseSever<SprintReminderResponse>
    remindOverdueTasks(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintProgressService
                        .remindOverdueTasks(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== REMIND BLOCKED =====================

    @Operation(
            summary = "Gửi nhắc nhở Task bị block trong Sprint"
    )
    @PostMapping("/notifications/remind-blocked-tasks")
    public ApiResponseSever<SprintReminderResponse>
    remindBlockedTasks(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintProgressService
                        .remindBlockedTasks(
                                projectId,
                                sprintId
                        )
        );
    }
}
