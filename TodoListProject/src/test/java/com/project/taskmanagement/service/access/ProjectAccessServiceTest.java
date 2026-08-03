package com.project.taskmanagement.service.access;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessServiceTest {

    @Mock
    ProjectRepository projectRepository;

    @Mock
    ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    ProjectAccessService projectAccessService;

    @Test
    void adminCanViewWithoutMembershipLookup() {
        Project project = TestDataFactory.activeProject();
        User admin = TestDataFactory.admin();

        assertThatCode(() -> projectAccessService.requireViewAccess(project, admin))
                .doesNotThrowAnyException();

        verify(projectMemberRepository, never())
                .existsByProjectIdAndUserId(project.getId(), admin.getId());
    }

    @Test
    void memberCanViewAndOutsiderIsDenied() {
        Project project = TestDataFactory.activeProject();
        User member = TestDataFactory.employee();
        User outsider = TestDataFactory.employee();

        when(projectMemberRepository.existsByProjectIdAndUserId(
                project.getId(),
                member.getId()
        )).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId(
                project.getId(),
                outsider.getId()
        )).thenReturn(false);

        assertThatCode(() -> projectAccessService.requireViewAccess(project, member))
                .doesNotThrowAnyException();
        assertErrorCode(
                () -> projectAccessService.requireViewAccess(project, outsider),
                ErrorCode.PROJECT_ACCESS_DENIED
        );
    }

    @Test
    void ownerProjectManagerAndScrumMasterCanManageSprint() {
        Project project = TestDataFactory.activeProject();
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));

        for (ProjectMemberRole role : new ProjectMemberRole[]{
                ProjectMemberRole.OWNER,
                ProjectMemberRole.PROJECT_MANAGER,
                ProjectMemberRole.SCRUM_MASTER
        }) {
            User user = TestDataFactory.employee();
            ProjectMember membership = TestDataFactory.membership(project, user, role);
            when(projectMemberRepository.findByProjectIdAndUserId(
                    project.getId(),
                    user.getId()
            )).thenReturn(Optional.of(membership));

            assertThat(projectAccessService.requireSprintManagementAccess(
                    project.getId(),
                    user
            )).isSameAs(membership);
        }
    }

    @Test
    void developerCannotManageSprint() {
        Project project = TestDataFactory.activeProject();
        User developer = TestDataFactory.employee();
        ProjectMember membership = TestDataFactory.membership(
                project,
                developer,
                ProjectMemberRole.DEVELOPER
        );

        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(
                project.getId(),
                developer.getId()
        )).thenReturn(Optional.of(membership));

        assertErrorCode(
                () -> projectAccessService.requireSprintManagementAccess(
                        project.getId(),
                        developer
                ),
                ErrorCode.SPRINT_ACCESS_DENIED
        );
    }

    @Test
    void everyProjectMemberCanAssignTask() {
        Project project = TestDataFactory.activeProject();
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));

        for (ProjectMemberRole role : new ProjectMemberRole[]{
                ProjectMemberRole.OWNER,
                ProjectMemberRole.PROJECT_MANAGER,
                ProjectMemberRole.SCRUM_MASTER,
                ProjectMemberRole.PRODUCT_OWNER,
                ProjectMemberRole.DEVELOPER,
                ProjectMemberRole.TESTER,
                ProjectMemberRole.VIEWER
        }) {
            User user = TestDataFactory.employee();
            ProjectMember membership = TestDataFactory.membership(project, user, role);
            when(projectMemberRepository.findByProjectIdAndUserId(
                    project.getId(),
                    user.getId()
            )).thenReturn(Optional.of(membership));

            assertThat(projectAccessService.requireTaskAssignmentAccess(
                    project.getId(),
                    user
            )).isSameAs(membership);
        }
    }

    @Test
    void everyProjectMemberCanManageBacklogUnderCurrentPolicy() {
        Project project = TestDataFactory.activeProject();
        User productOwner = TestDataFactory.employee();
        ProjectMember membership = TestDataFactory.membership(
                project,
                productOwner,
                ProjectMemberRole.PRODUCT_OWNER
        );

        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(
                project.getId(),
                productOwner.getId()
        )).thenReturn(Optional.of(membership));

        assertThat(projectAccessService.requireBacklogManagementAccess(
                project.getId(),
                productOwner
        )).isSameAs(membership);
    }

    @Test
    void adminCannotExecuteProjectBusinessOperations() {
        Project project = TestDataFactory.activeProject();
        User admin = TestDataFactory.admin();
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));

        assertErrorCode(
                () -> projectAccessService.requireSprintManagementAccess(
                        project.getId(),
                        admin
                ),
                ErrorCode.SPRINT_ACCESS_DENIED
        );
        assertErrorCode(
                () -> projectAccessService.requireBacklogManagementAccess(
                        project.getId(),
                        admin
                ),
                ErrorCode.BACKLOG_ACCESS_DENIED
        );
        assertErrorCode(
                () -> projectAccessService.requireTaskManagementAccess(
                        project.getId(),
                        admin
                ),
                ErrorCode.TASK_ACCESS_DENIED
        );
        assertErrorCode(
                () -> projectAccessService.requireTaskAssignmentAccess(
                        project.getId(),
                        admin
                ),
                ErrorCode.TASK_ASSIGN_ACCESS_DENIED
        );
    }

    private void assertErrorCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
            ErrorCode expected
    ) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(
                        ((BusinessException) error).getErrorCode()
                ).isEqualTo(expected));
    }
}
