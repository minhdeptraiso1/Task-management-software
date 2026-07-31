package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.TaskImportWriterService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import com.project.taskmanagement.service.model.TaskImportRowData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskImportWriterServiceImpl
        implements TaskImportWriterService {

    TaskRepository taskRepository;
    TaskImportBatchRepository taskImportBatchRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    ProjectActivityService projectActivityService;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.ADMIN_IMPORT_AUDIT_SEARCH,
                    allEntries = true
            )
    })
    public List<UUID> importAll(
            UUID projectId,
            UUID sprintId,
            UUID actorUserId,
            List<TaskImportRowData> rows,
            TaskImportBatch batch
    ) {
        Map<String, Object> oldValue =
                importAuditValue(batch);

        batch.setStatus(
                TaskImportStatus.IMPORTING
        );

        taskImportBatchRepository.save(batch);

        Map<String, UUID> memberIdByEmail =
                loadMemberIdByEmail(projectId);

        /*
         * Mỗi trạng thái/cột Kanban có position riêng.
         * Task mới đều ở TODO.
         */
        Long maxPosition =
                taskRepository.findMaxSprintPosition(
                        projectId,
                        sprintId,
                        TaskStatus.TODO
                );

        long nextPosition =
                maxPosition == null
                        ? 1L
                        : maxPosition + 1L;

        List<Task> tasks =
                new ArrayList<>();

        for (TaskImportRowData row : rows) {
            UUID assigneeId = null;

            if (row.assigneeEmail() != null
                    && !row.assigneeEmail().isBlank()) {

                assigneeId =
                        memberIdByEmail.get(
                                normalizeEmail(
                                        row.assigneeEmail()
                                )
                        );
            }

            Task task =
                    Task.builder()
                            .projectId(projectId)
                            .backlogItemId(
                                    row.backlogItemId()
                            )
                            .currentSprintId(sprintId)
                            .originSprintId(sprintId)
                            .title(row.taskTitle())
                            .description(
                                    row.description()
                            )
                            .type(row.type())
                            .status(TaskStatus.TODO)
                            .priority(row.priority())
                            .assigneeUserId(assigneeId)
                            .reporterUserId(actorUserId)
                            .estimatedMinutes(
                                    row.estimatedMinutes()
                            )
                            .startDate(row.startDate())
                            .dueDate(row.dueDate())
                            .completedAt(null)
                            .position(nextPosition++)
                            .build();

            tasks.add(task);
        }

        List<Task> savedTasks =
                taskRepository.saveAll(tasks);

        /*
         * Buộc Hibernate thực thi toàn bộ INSERT tại đây.
         * Nếu một Task lỗi, transaction rollback toàn bộ.
         */
        taskRepository.flush();

        for (Task task : savedTasks) {
            Map<String, Object> value =
                    new LinkedHashMap<>();

            value.put(
                    "title",
                    task.getTitle()
            );

            value.put(
                    "backlogItemId",
                    task.getBacklogItemId()
            );

            value.put(
                    "currentSprintId",
                    task.getCurrentSprintId()
            );

            value.put(
                    "status",
                    task.getStatus()
            );

            value.put(
                    "priority",
                    task.getPriority()
            );

            value.put(
                    "assigneeUserId",
                    task.getAssigneeUserId()
            );

            value.put(
                    "importBatchId",
                    batch.getId()
            );

            projectActivityService.log(
                    new ProjectActivityCommand(
                            projectId,
                            ActivityEntityType.TASK,
                            task.getId(),
                            ProjectActivityAction.TASK_IMPORTED,
                            actorUserId,
                            null,
                            value
                    )
            );
        }

        batch.setStatus(
                TaskImportStatus.COMPLETED
        );

        batch.setSuccessRows(
                savedTasks.size()
        );

        batch.setFailedRows(0);

        batch.setCompletedAt(
                Instant.now()
        );

        batch.setErrorMessage(null);

        taskImportBatchRepository.save(batch);

        systemAuditService.log(
                new SystemAuditCommand(
                        actorUserId,
                        SystemAuditAction.IMPORT_COMPLETED,
                        SystemAuditResourceType.TASK_IMPORT_BATCH,
                        batch.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        oldValue,
                        importAuditValue(batch),
                        true,
                        null
                )
        );

        return savedTasks
                .stream()
                .map(Task::getId)
                .toList();
    }

    private Map<String, UUID> loadMemberIdByEmail(
            UUID projectId
    ) {
        List<ProjectMember> projectMembers =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        Map<String, UUID> result =
                new HashMap<>();

        for (ProjectMember member : projectMembers) {
            User user =
                    userRepository
                            .findById(
                                    member.getUserId()
                            )
                            .orElse(null);

            if (user == null
                    || user.getEmail() == null
                    || user.getEmail().isBlank()) {
                continue;
            }

            result.put(
                    normalizeEmail(user.getEmail()),
                    user.getId()
            );
        }

        return result;
    }

    private Map<String, Object> importAuditValue(
            TaskImportBatch batch
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put("batchId", batch.getId());
        value.put("projectId", batch.getProjectId());
        value.put("sprintId", batch.getSprintId());
        value.put("fileName", batch.getOriginalFileName());
        value.put("status", batch.getStatus());
        value.put("totalRows", batch.getTotalRows());
        value.put("successRows", batch.getSuccessRows());
        value.put("failedRows", batch.getFailedRows());
        value.put("importedByUserId", batch.getImportedByUserId());
        value.put("startedAt", batch.getStartedAt());
        value.put("completedAt", batch.getCompletedAt());
        value.put("errorMessage", batch.getErrorMessage());

        return value;
    }

    private String normalizeEmail(
            String email
    ) {
        return email
                .trim()
                .toLowerCase();
    }
}
