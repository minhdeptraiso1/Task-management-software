package com.project.taskmanagement.security;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.BaseIntegrationTest;
import com.project.taskmanagement.integration.support.TestDataFactory;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import com.project.taskmanagement.security.LoginRateLimiter;
import com.project.taskmanagement.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

public abstract class BaseSecurityIT extends BaseIntegrationTest {

    @Autowired
    protected TestDataFactory testDataFactory;
    @Autowired
    protected ProjectRepository projectRepository;
    @Autowired
    protected ProjectMemberRepository projectMemberRepository;
    @Autowired
    protected SprintRepository sprintRepository;
    @Autowired
    protected BacklogItemRepository backlogItemRepository;
    @Autowired
    protected TaskRepository taskRepository;
    @MockitoBean
    protected LoginRateLimiter loginRateLimiter;
    @MockitoBean
    protected RateLimitService rateLimitService;

    protected User admin;
    protected User manager;
    protected User employee;
    protected User otherMember;
    protected User outsider;

    protected String adminToken;
    protected String managerToken;
    protected String employeeToken;
    protected String otherMemberToken;
    protected String outsiderToken;

    @BeforeEach
    void createSecurityUsersAndTokens() throws Exception {
        admin = createUser("security-admin", UserRole.ADMIN);
        manager = createUser("security-manager", UserRole.MANAGER);
        employee = createUser("security-employee", UserRole.EMPLOYEE);
        otherMember = createUser("security-other-member", UserRole.EMPLOYEE);
        outsider = createUser("security-outsider", UserRole.EMPLOYEE);

        adminToken = login(admin, "127.0.0.11");
        managerToken = login(manager, "127.0.0.12");
        employeeToken = login(employee, "127.0.0.13");
        otherMemberToken = login(otherMember, "127.0.0.14");
        outsiderToken = login(outsider, "127.0.0.15");
        SecurityContextHolder.clearContext();
    }

    protected ProjectTaskFixture createProjectTaskFixture() {
        authenticateForAuditing(manager);

        Project project = projectRepository.saveAndFlush(
                TestEntityFactory.project(
                        "SECURITY",
                        "Security regression project",
                        manager.getId()
                )
        );
        projectMemberRepository.save(
                TestEntityFactory.projectMember(
                        project.getId(),
                        manager.getId(),
                        ProjectMemberRole.OWNER
                )
        );
        projectMemberRepository.save(
                TestEntityFactory.projectMember(
                        project.getId(),
                        employee.getId(),
                        ProjectMemberRole.DEVELOPER
                )
        );
        projectMemberRepository.saveAndFlush(
                TestEntityFactory.projectMember(
                        project.getId(),
                        otherMember.getId(),
                        ProjectMemberRole.DEVELOPER
                )
        );

        Sprint sprint = TestEntityFactory.sprint(
                project.getId(),
                "Security Sprint",
                SprintStatus.ACTIVE,
                manager.getId()
        );
        sprint.setStartedAt(Instant.now());
        sprint = sprintRepository.saveAndFlush(sprint);

        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(),
                        sprint.getId(),
                        "Security story",
                        1L,
                        manager.getId()
                )
        );
        Task task = TestEntityFactory.task(
                project.getId(),
                backlogItem.getId(),
                sprint.getId(),
                "Secured task",
                TaskStatus.TODO,
                1L,
                manager.getId()
        );
        task.setAssigneeUserId(employee.getId());
        task = taskRepository.saveAndFlush(task);
        SecurityContextHolder.clearContext();

        return new ProjectTaskFixture(project, sprint, backlogItem, task);
    }

    protected String authorization(String token) {
        return authTestHelper.bearer(token);
    }

    protected void authenticateForAuditing(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                )
        );
    }

    private User createUser(String username, UserRole role) {
        return testDataFactory.createUser(
                username,
                username + "@example.com",
                role
        );
    }

    private String login(User user, String remoteAddress) throws Exception {
        return authTestHelper.loginAndGetAccessToken(
                mockMvc,
                user.getEmail(),
                TestDataFactory.DEFAULT_PASSWORD,
                remoteAddress
        );
    }

    protected record ProjectTaskFixture(
            Project project,
            Sprint sprint,
            BacklogItem backlogItem,
            Task task
    ) {
    }
}
