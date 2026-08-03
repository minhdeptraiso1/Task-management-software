package com.project.taskmanagement.integration.atomic;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SprintService;
import com.project.taskmanagement.service.TaskSprintSyncService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Test-only transaction boundary used to prove that nested write services join
 * the caller transaction and leave no partial business data after a failure.
 */
@TestComponent
public class AtomicFailureSimulationService {

    private final SprintService sprintService;
    private final TaskSprintSyncService taskSprintSyncService;
    private final TaskRepository taskRepository;
    private final ProjectActivityService projectActivityService;
    private final NotificationService notificationService;

    public AtomicFailureSimulationService(
            SprintService sprintService,
            TaskSprintSyncService taskSprintSyncService,
            TaskRepository taskRepository,
            ProjectActivityService projectActivityService,
            NotificationService notificationService
    ) {
        this.sprintService = sprintService;
        this.taskSprintSyncService = taskSprintSyncService;
        this.taskRepository = taskRepository;
        this.projectActivityService = projectActivityService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void cancelSprintThenFail(
            UUID projectId,
            UUID sprintId
    ) {
        sprintService.cancel(projectId, sprintId);
        failAfterDatabaseWrites();
    }

    @Transactional
    public void completeSprintThenFail(
            UUID projectId,
            UUID sprintId
    ) {
        sprintService.complete(projectId, sprintId);
        failAfterDatabaseWrites();
    }

    @Transactional
    public void detachTasksThenFail(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            UUID actorUserId
    ) {
        taskSprintSyncService.detachBacklogItemTasksFromSprint(
                projectId,
                backlogItemId,
                sprintId,
                actorUserId
        );
        failAfterDatabaseWrites();
    }

    @Transactional
    public void createCollaborationDataThenFail(
            ProjectActivityCommand activityCommand,
            NotificationCommand notificationCommand
    ) {
        projectActivityService.log(activityCommand);
        notificationService.create(notificationCommand);
        failAfterDatabaseWrites();
    }

    @Transactional
    public void softDeleteTaskThenFail(
            UUID projectId,
            UUID taskId,
            String deletedBy
    ) {
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        task.markDeleted(deletedBy);
        taskRepository.saveAndFlush(task);
        failAfterDatabaseWrites();
    }

    private void failAfterDatabaseWrites() {
        throw new AtomicFailureException("Simulated failure after database writes");
    }

    public static final class AtomicFailureException extends RuntimeException {
        public AtomicFailureException(String message) {
            super(message);
        }
    }
}
