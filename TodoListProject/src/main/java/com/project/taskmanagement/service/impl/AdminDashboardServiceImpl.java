package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.admin.AdminAttachmentSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminBugSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminDashboardResponse;
import com.project.taskmanagement.dto.response.admin.AdminProjectSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminSprintSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminSystemSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminTaskSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminUserSummaryResponse;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.AttachmentRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.AdminDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminDashboardServiceImpl
        implements AdminDashboardService {

    UserRepository userRepository;
    ProjectRepository projectRepository;
    SprintRepository sprintRepository;
    TaskRepository taskRepository;
    BugRepository bugRepository;
    AttachmentRepository attachmentRepository;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_DASHBOARD,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()"
    )
    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
                buildUserSummary(),
                buildProjectSummary(),
                buildSprintSummary(),
                buildTaskSummary(),
                buildBugSummary(),
                buildAttachmentSummary(),
                buildSystemSummary()
        );
    }

    private AdminUserSummaryResponse buildUserSummary() {
        return new AdminUserSummaryResponse(
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                userRepository.countByEnabledFalse(),
                userRepository.countByRole(UserRole.ADMIN),
                userRepository.countByRole(UserRole.MANAGER),
                userRepository.countByRole(UserRole.EMPLOYEE)
        );
    }

    private AdminProjectSummaryResponse buildProjectSummary() {
        return new AdminProjectSummaryResponse(
                projectRepository.count(),
                projectRepository.countByStatus(ProjectStatus.PLANNING),
                projectRepository.countByStatus(ProjectStatus.ACTIVE),
                projectRepository.countByStatus(ProjectStatus.ON_HOLD),
                projectRepository.countByStatus(ProjectStatus.COMPLETED),
                projectRepository.countByStatus(ProjectStatus.CANCELLED),
                projectRepository.countByStatus(ProjectStatus.ARCHIVED)
        );
    }

    private AdminSprintSummaryResponse buildSprintSummary() {
        return new AdminSprintSummaryResponse(
                sprintRepository.count(),
                sprintRepository.countByStatus(SprintStatus.PLANNING),
                sprintRepository.countByStatus(SprintStatus.ACTIVE),
                sprintRepository.countByStatus(SprintStatus.COMPLETED),
                sprintRepository.countByStatus(SprintStatus.CANCELLED)
        );
    }

    private AdminTaskSummaryResponse buildTaskSummary() {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        List<TaskStatus> finishedStatuses =
                List.of(
                        TaskStatus.DONE,
                        TaskStatus.CANCELLED
                );

        return new AdminTaskSummaryResponse(
                taskRepository.count(),
                taskRepository.countByStatus(TaskStatus.TODO),
                taskRepository.countByStatus(TaskStatus.IN_PROGRESS),
                taskRepository.countByStatus(TaskStatus.IN_REVIEW),
                taskRepository.countByStatus(TaskStatus.BLOCKED),
                taskRepository.countByStatus(TaskStatus.DONE),
                taskRepository.countByStatus(TaskStatus.CANCELLED),
                taskRepository.countByDueDateBeforeAndStatusNotIn(
                        today,
                        finishedStatuses
                )
        );
    }

    private AdminBugSummaryResponse buildBugSummary() {
        return new AdminBugSummaryResponse(
                bugRepository.count(),
                bugRepository.countByStatus(BugStatus.OPEN),
                bugRepository.countByStatus(BugStatus.IN_PROGRESS),
                bugRepository.countByStatus(BugStatus.RESOLVED),
                bugRepository.countByStatus(BugStatus.CLOSED),
                bugRepository.countBySeverity(BugSeverity.CRITICAL)
        );
    }

    private AdminAttachmentSummaryResponse buildAttachmentSummary() {
        return new AdminAttachmentSummaryResponse(
                attachmentRepository.count(),
                safeLong(attachmentRepository.sumTotalSizeBytes())
        );
    }

    private AdminSystemSummaryResponse buildSystemSummary() {
        return new AdminSystemSummaryResponse(
                Instant.now(),
                BUSINESS_ZONE.toString()
        );
    }

    private long safeLong(Number value) {
        return value == null
                ? 0L
                : value.longValue();
    }
}
