package com.project.taskmanagement.integration.atomic;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.BaseIntegrationTest;
import com.project.taskmanagement.integration.support.TestDataFactory;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(AtomicFailureSimulationService.class)
class AtomicTransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AtomicFailureSimulationService failureSimulationService;
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
    private TaskRepository taskRepository;
    @Autowired
    private ProjectActivityLogRepository activityRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationRecipientRepository recipientRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void cancelSprint_rollsBackSprintBacklogTasksActivityAndNotification() {
        Scenario scenario = createScenario(
                SprintStatus.ACTIVE,
                BacklogItemStatus.IN_SPRINT,
                TaskStatus.IN_PROGRESS
        );

        assertThatThrownBy(() -> failureSimulationService.cancelSprintThenFail(
                scenario.project().getId(),
                scenario.sprint().getId()
        )).isInstanceOf(AtomicFailureSimulationService.AtomicFailureException.class);

        clearPersistenceContext();
        Sprint sprint = sprintRepository.findById(scenario.sprint().getId()).orElseThrow();
        BacklogItem item = backlogItemRepository.findById(scenario.backlogItem().getId()).orElseThrow();
        Task task = taskRepository.findById(scenario.task().getId()).orElseThrow();

        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(sprint.getCompletedAt()).isNull();
        assertThat(item.getSprintId()).isEqualTo(sprint.getId());
        assertThat(item.getStatus()).isEqualTo(BacklogItemStatus.IN_SPRINT);
        assertThat(task.getCurrentSprintId()).isEqualTo(sprint.getId());
        assertThat(task.getOriginSprintId()).isEqualTo(sprint.getId());
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertNoCollaborationDataWasCommitted();
    }

    @Test
    void completeSprint_rollsBackStatusActivityAndNotification() {
        Scenario scenario = createScenario(
                SprintStatus.ACTIVE,
                BacklogItemStatus.DONE,
                TaskStatus.DONE
        );

        assertThatThrownBy(() -> failureSimulationService.completeSprintThenFail(
                scenario.project().getId(),
                scenario.sprint().getId()
        )).isInstanceOf(AtomicFailureSimulationService.AtomicFailureException.class);

        clearPersistenceContext();
        Sprint sprint = sprintRepository.findById(scenario.sprint().getId()).orElseThrow();

        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(sprint.getCompletedAt()).isNull();
        assertNoCollaborationDataWasCommitted();
    }

    @Test
    void taskSprintDetach_rollsBackSprintStatusOriginAndMoveActivity() {
        Scenario scenario = createScenario(
                SprintStatus.ACTIVE,
                BacklogItemStatus.IN_SPRINT,
                TaskStatus.IN_REVIEW
        );

        assertThatThrownBy(() -> failureSimulationService.detachTasksThenFail(
                scenario.project().getId(),
                scenario.backlogItem().getId(),
                scenario.sprint().getId(),
                scenario.actor().getId()
        )).isInstanceOf(AtomicFailureSimulationService.AtomicFailureException.class);

        clearPersistenceContext();
        Task task = taskRepository.findById(scenario.task().getId()).orElseThrow();

        assertThat(task.getCurrentSprintId()).isEqualTo(scenario.sprint().getId());
        assertThat(task.getOriginSprintId()).isEqualTo(scenario.sprint().getId());
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_REVIEW);
        assertThat(task.getPosition()).isEqualTo(1L);
        assertThat(activityRepository.count()).isZero();
    }

    @Test
    void activityAndNotification_joinOuterTransactionAndRollbackTogether() {
        Scenario scenario = createScenario(
                SprintStatus.ACTIVE,
                BacklogItemStatus.IN_SPRINT,
                TaskStatus.TODO
        );

        ProjectActivityCommand activity = new ProjectActivityCommand(
                scenario.project().getId(),
                ActivityEntityType.TASK,
                scenario.task().getId(),
                ProjectActivityAction.TASK_UPDATED,
                scenario.actor().getId(),
                null,
                java.util.Map.of("title", "Updated in failed transaction")
        );
        NotificationCommand notification = new NotificationCommand(
                NotificationType.TASK_STATUS_CHANGED,
                "Atomic rollback test",
                "This notification must not be committed",
                scenario.actor().getId(),
                scenario.project().getId(),
                ActivityEntityType.TASK,
                scenario.task().getId(),
                List.of(scenario.recipient().getId())
        );

        assertThatThrownBy(() -> failureSimulationService.createCollaborationDataThenFail(
                activity,
                notification
        )).isInstanceOf(AtomicFailureSimulationService.AtomicFailureException.class);

        clearPersistenceContext();
        assertNoCollaborationDataWasCommitted();
    }

    @Test
    void softDeleteTask_isRolledBackWhenLaterWriteFails() {
        Scenario scenario = createScenario(
                SprintStatus.ACTIVE,
                BacklogItemStatus.IN_SPRINT,
                TaskStatus.TODO
        );

        assertThatThrownBy(() -> failureSimulationService.softDeleteTaskThenFail(
                scenario.project().getId(),
                scenario.task().getId(),
                scenario.actor().getUsername()
        )).isInstanceOf(AtomicFailureSimulationService.AtomicFailureException.class);

        clearPersistenceContext();
        Task task = taskRepository.findById(scenario.task().getId()).orElseThrow();
        assertThat(task.getDeletedAt()).isNull();
        assertThat(task.getDeletedBy()).isNull();
    }

    private Scenario createScenario(
            SprintStatus sprintStatus,
            BacklogItemStatus backlogStatus,
            TaskStatus taskStatus
    ) {
        User actor = testDataFactory.createUser(
                "atomic-manager",
                "atomic-manager@example.com",
                UserRole.MANAGER
        );
        User recipient = testDataFactory.createUser(
                "atomic-member",
                "atomic-member@example.com",
                UserRole.EMPLOYEE
        );
        authenticate(actor);

        Project project = projectRepository.saveAndFlush(
                TestEntityFactory.project("ATOMIC", "Atomic transaction project", actor.getId())
        );
        projectMemberRepository.save(
                TestEntityFactory.projectMember(project.getId(), actor.getId(), ProjectMemberRole.OWNER)
        );
        projectMemberRepository.saveAndFlush(
                TestEntityFactory.projectMember(project.getId(), recipient.getId(), ProjectMemberRole.DEVELOPER)
        );

        Sprint sprint = TestEntityFactory.sprint(
                project.getId(),
                "Atomic Sprint",
                sprintStatus,
                actor.getId()
        );
        if (sprintStatus == SprintStatus.ACTIVE) {
            sprint.setStartedAt(java.time.Instant.now());
        }
        sprint = sprintRepository.saveAndFlush(sprint);

        BacklogItem backlogItem = TestEntityFactory.backlogItem(
                project.getId(),
                sprint.getId(),
                "Atomic backlog item",
                1L,
                actor.getId()
        );
        backlogItem.setStatus(backlogStatus);
        backlogItem = backlogItemRepository.saveAndFlush(backlogItem);

        Task task = TestEntityFactory.task(
                project.getId(),
                backlogItem.getId(),
                sprint.getId(),
                "Atomic task",
                taskStatus,
                1L,
                actor.getId()
        );
        task = taskRepository.saveAndFlush(task);

        return new Scenario(actor, recipient, project, sprint, backlogItem, task);
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

    private void assertNoCollaborationDataWasCommitted() {
        assertThat(activityRepository.count()).isZero();
        assertThat(notificationRepository.count()).isZero();
        assertThat(recipientRepository.count()).isZero();
    }

    private void clearPersistenceContext() {
        entityManager.clear();
    }

    private record Scenario(
            User actor,
            User recipient,
            Project project,
            Sprint sprint,
            BacklogItem backlogItem,
            Task task
    ) {
    }
}
