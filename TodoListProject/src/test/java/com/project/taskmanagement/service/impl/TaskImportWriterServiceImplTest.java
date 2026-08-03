package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.model.TaskImportRowData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class TaskImportWriterServiceImplTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final TaskImportBatchRepository batchRepository = mock(TaskImportBatchRepository.class);
    private final ProjectMemberRepository memberRepository = mock(ProjectMemberRepository.class);
    private final UserLookupHelper userLookupHelper = mock(UserLookupHelper.class);
    private final ProjectActivityService activityService = mock(ProjectActivityService.class);
    private final SystemAuditService auditService = mock(SystemAuditService.class);
    private final AuditRequestHelper auditRequestHelper = mock(AuditRequestHelper.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    private final TaskImportWriterServiceImpl service = new TaskImportWriterServiceImpl(
            taskRepository,
            batchRepository,
            memberRepository,
            userLookupHelper,
            activityService,
            auditService,
            auditRequestHelper,
            request
    );

    @Test
    void importAllSavesAndFlushesOnceAndWritesOneAggregateActivity() {
        UUID projectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(memberId)
                .build();
        User memberUser = User.builder()
                .username("dev")
                .email("dev@example.com")
                .enabled(true)
                .build();
        TaskImportBatch batch = TaskImportBatch.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .importedByUserId(actorId)
                .originalFileName("tasks.xlsx")
                .status(TaskImportStatus.VALIDATING)
                .totalRows(2)
                .build();
        List<TaskImportRowData> rows = List.of(
                row(UUID.randomUUID(), "Task A", "dev@example.com"),
                row(UUID.randomUUID(), "Task B", null)
        );

        when(memberRepository.findAllByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(member));
        when(userLookupHelper.findUserMap(List.of(memberId)))
                .thenReturn(Map.of(memberId, memberUser));
        when(userLookupHelper.getOrNull(anyMap(), eq(memberId))).thenReturn(memberUser);
        when(taskRepository.findMaxSprintPosition(projectId, sprintId, com.project.taskmanagement.enums.TaskStatus.TODO))
                .thenReturn(4L);
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(batchRepository.save(any(TaskImportBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<UUID> importedIds = service.importAll(projectId, sprintId, actorId, rows, batch);

        assertThat(importedIds).hasSize(2);
        assertThat(batch.getStatus()).isEqualTo(TaskImportStatus.COMPLETED);
        assertThat(batch.getSuccessRows()).isEqualTo(2);
        verify(taskRepository, times(1)).saveAll(anyList());
        verify(taskRepository, times(1)).flush();

        ArgumentCaptor<ProjectActivityCommand> activityCaptor =
                ArgumentCaptor.forClass(ProjectActivityCommand.class);
        verify(activityService, times(1)).log(activityCaptor.capture());
        assertThat(activityCaptor.getValue().entityType()).isEqualTo(ActivityEntityType.TASK_IMPORT);
        assertThat(activityCaptor.getValue().entityId()).isEqualTo(batch.getId());
        @SuppressWarnings("unchecked")
        Map<String, Object> activityValue =
                (Map<String, Object>) activityCaptor.getValue().newValue();
        assertThat(activityValue)
                .containsEntry("sprintId", sprintId)
                .containsEntry("batchId", batch.getId())
                .containsEntry("totalTasks", 2);
    }

    @Test
    void persistenceFailureStopsBeforeFlushActivityAndCompletion() {
        UUID projectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        TaskImportBatch batch = TaskImportBatch.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .importedByUserId(actorId)
                .originalFileName("tasks.xlsx")
                .status(TaskImportStatus.VALIDATING)
                .totalRows(1)
                .build();

        when(memberRepository.findAllByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of());
        when(userLookupHelper.findUserMap(List.of())).thenReturn(Map.of());
        when(taskRepository.findMaxSprintPosition(
                projectId,
                sprintId,
                com.project.taskmanagement.enums.TaskStatus.TODO
        )).thenReturn(0L);
        when(taskRepository.saveAll(anyList()))
                .thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> service.importAll(
                projectId,
                sprintId,
                actorId,
                List.of(row(UUID.randomUUID(), "Task A", null)),
                batch
        )).isInstanceOf(IllegalStateException.class);

        verify(taskRepository, never()).flush();
        verifyNoInteractions(activityService, auditService);
        assertThat(batch.getStatus()).isEqualTo(TaskImportStatus.IMPORTING);
        assertThat(batch.getCompletedAt()).isNull();
    }

    private TaskImportRowData row(UUID backlogItemId, String title, String email) {
        return new TaskImportRowData(
                2,
                backlogItemId,
                "Story",
                title,
                null,
                TaskType.DEVELOPMENT,
                TaskPriority.MEDIUM,
                email,
                60,
                null,
                null
        );
    }
}
