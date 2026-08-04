package com.project.taskmanagement.controller.admin;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.scheduler.DailyDigestRunResponse;
import com.project.taskmanagement.service.DailyDigestService;
import com.project.taskmanagement.service.TaskDueReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = OpenApiTags.ADMIN, description = "Kích hoạt thủ công scheduler dành cho ADMIN")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/schedulers")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminSchedulerController {

    TaskDueReminderService taskDueReminderService;
    DailyDigestService dailyDigestService;

    @Operation(
            summary = "Chạy thủ công Task due reminder"
    )
    @PostMapping("/task-due-reminders/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<Void> runTaskDueReminder() {
        taskDueReminderService.sendDueSoonReminders();
        taskDueReminderService.sendOverdueReminders();

        return ApiResponseSever.ok();
    }

    @Operation(
            summary = "Chạy Daily Digest thủ công"
    )
    @PostMapping("/daily-digest/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<DailyDigestRunResponse> runDailyDigest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate businessDate
    ) {
        return ApiResponseSever.ok(
                dailyDigestService.runDailyDigest(
                        businessDate
                )
        );
    }
}
