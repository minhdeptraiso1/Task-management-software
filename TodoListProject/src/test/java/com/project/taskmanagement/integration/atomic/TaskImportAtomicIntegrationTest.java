package com.project.taskmanagement.integration.atomic;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.BaseIntegrationTest;
import com.project.taskmanagement.integration.support.TestDataFactory;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.TaskImportWriterService;
import com.project.taskmanagement.service.model.TaskImportRowData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class TaskImportAtomicIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TaskImportWriterService taskImportWriterService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private BacklogItemRepository backlogItemRepository;
    @Autowired
    private TaskImportBatchRepository batchRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectActivityLogRepository activityRepository;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SystemAuditService systemAuditService;

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void importWriter_rollsBackEveryTaskActivityAndBatchUpdate_whenFinalWriteFails() {
        ImportScenario scenario = createImportScenario();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        doThrow(new IllegalStateException("Simulated audit persistence failure"))
                .when(systemAuditService)
                .log(any());

        List<TaskImportRowData> rows = List.of(
                row(2, scenario.backlogItem(), "Imported task A"),
                row(3, scenario.backlogItem(), "Imported task B")
        );

        assertThatThrownBy(() -> taskImportWriterService.importAll(
                scenario.project().getId(),
                scenario.sprint().getId(),
                scenario.actor().getId(),
                rows,
                scenario.batch()
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulated audit persistence failure");

        entityManager.clear();
        TaskImportBatch persistedBatch = batchRepository.findById(scenario.batch().getId()).orElseThrow();

        assertThat(taskRepository.countByProjectIdAndCurrentSprintId(
                scenario.project().getId(),
                scenario.sprint().getId()
        )).isZero();
        assertThat(activityRepository.count()).isZero();
        assertThat(persistedBatch.getStatus()).isEqualTo(TaskImportStatus.VALIDATING);
        assertThat(persistedBatch.getSuccessRows()).isZero();
        assertThat(persistedBatch.getCompletedAt()).isNull();
    }

    private ImportScenario createImportScenario() {
        User actor = testDataFactory.createUser(
                "import-manager",
                "import-manager@example.com",
                UserRole.MANAGER
        );
        authenticate(actor);

        Project project = projectRepository.saveAndFlush(
                TestEntityFactory.project("IMPORT-ATOMIC", "Import atomic project", actor.getId())
        );
        projectMemberRepository.saveAndFlush(
                TestEntityFactory.projectMember(project.getId(), actor.getId(), ProjectMemberRole.OWNER)
        );
        Sprint sprint = sprintRepository.saveAndFlush(
                TestEntityFactory.sprint(
                        project.getId(),
                        "Import Sprint",
                        SprintStatus.PLANNING,
                        actor.getId()
                )
        );
        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(),
                        sprint.getId(),
                        "Import target story",
                        1L,
                        actor.getId()
                )
        );
        TaskImportBatch batch = batchRepository.saveAndFlush(
                TaskImportBatch.builder()
                        .projectId(project.getId())
                        .sprintId(sprint.getId())
                        .importedByUserId(actor.getId())
                        .originalFileName("atomic-import.xlsx")
                        .status(TaskImportStatus.VALIDATING)
                        .totalRows(2)
                        .successRows(0)
                        .failedRows(0)
                        .startedAt(Instant.now())
                        .build()
        );

        return new ImportScenario(actor, project, sprint, backlogItem, batch);
    }

    private TaskImportRowData row(
            int rowNumber,
            BacklogItem backlogItem,
            String title
    ) {
        return new TaskImportRowData(
                rowNumber,
                backlogItem.getId(),
                backlogItem.getTitle(),
                title,
                "Atomic import test row",
                TaskType.DEVELOPMENT,
                TaskPriority.MEDIUM,
                null,
                60,
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 4)
        );
    }

    private void authenticate(User actor) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        actor.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + actor.getRole().name()))
                )
        );
    }

    private record ImportScenario(
            User actor,
            Project project,
            Sprint sprint,
            BacklogItem backlogItem,
            TaskImportBatch batch
    ) {
    }
}
