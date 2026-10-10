package com.project.taskmanagement.integration;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.support.TestDataFactory;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Test
    void manager_shouldCreateProjectSuccessfully()
            throws Exception {
        testDataFactory.createUser(
                "manager",
                "manager.project@test.com",
                UserRole.MANAGER
        );

        String token = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "manager.project@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        mockMvc.perform(
                        post("/projects")
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(projectBody("AGILE01", "Agile Project"))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.data.code").value("AGILE01"))
                .andExpect(jsonPath("$.data.name").value("Agile Project"))
                .andExpect(jsonPath("$.data.currentUserRole").value("OWNER"));
    }

    @Test
    void employee_shouldNotCreateProject()
            throws Exception {
        testDataFactory.createUser(
                "employee",
                "employee.project@test.com",
                UserRole.EMPLOYEE
        );

        String token = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "employee.project@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        mockMvc.perform(
                        post("/projects")
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(projectBody("AGILE02", "Forbidden Project"))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/projects"));
    }

    @Test
    void admin_shouldCreateProjectAndAssignManagerAsOwner()
            throws Exception {
        testDataFactory.createUser(
                "admin",
                "admin.project@test.com",
                UserRole.ADMIN
        );
        User manager = testDataFactory.createUser(
                "assigned_manager",
                "assigned.manager@test.com",
                UserRole.MANAGER
        );

        String token = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "admin.project@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        mockMvc.perform(
                        post("/projects")
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(projectBody(
                                        "ADMIN01",
                                        "Admin Assigned Project",
                                        manager.getId()
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ADMIN01"))
                .andExpect(jsonPath("$.data.currentUserRole").doesNotExist());

        Project project = projectRepository
                .findByCodeIgnoreCase("ADMIN01")
                .orElseThrow();
        ProjectMember owner = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), manager.getId())
                .orElseThrow();

        assertThat(owner.getRole()).isEqualTo(ProjectMemberRole.OWNER);
        assertThat(projectMemberRepository.countByProjectId(project.getId()))
                .isEqualTo(1);
    }

    @Test
    void admin_shouldNotCreateProjectWithoutAssignedManager()
            throws Exception {
        testDataFactory.createUser(
                "admin_no_owner",
                "admin.no.owner@test.com",
                UserRole.ADMIN
        );

        String token = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "admin.no.owner@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        mockMvc.perform(
                        post("/projects")
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(projectBody("ADMIN02", "Missing Owner Project"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("ADMIN phải chọn tài khoản MANAGER nhận dự án"));
    }

    @Test
    void admin_shouldFilterProjectsByAssignedManager()
            throws Exception {
        testDataFactory.createUser(
                "filter_admin",
                "filter.admin@test.com",
                UserRole.ADMIN
        );
        User firstManager = testDataFactory.createUser(
                "first_manager",
                "first.manager@test.com",
                UserRole.MANAGER
        );
        User secondManager = testDataFactory.createUser(
                "second_manager",
                "second.manager@test.com",
                UserRole.MANAGER
        );

        String token = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "filter.admin@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        mockMvc.perform(post("/projects")
                        .header("Authorization", authTestHelper.bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody("OWNER01", "First Manager Project", firstManager.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/projects")
                        .header("Authorization", authTestHelper.bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody("OWNER02", "Second Manager Project", secondManager.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/projects")
                        .header("Authorization", authTestHelper.bearer(token))
                        .param("managerUserId", firstManager.getId().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("OWNER01"));
    }

    private String projectBody(
            String code,
            String name
    ) throws Exception {
        return projectBody(code, name, null);
    }

    private String projectBody(
            String code,
            String name,
            UUID ownerUserId
    ) throws Exception {
        return json(
                new ProjectBody(
                        code,
                        name,
                        "Project integration test",
                        LocalDate.now(),
                        LocalDate.now().plusMonths(6),
                        ownerUserId
                )
        );
    }

    private record ProjectBody(
            String code,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            UUID ownerUserId
    ) {
    }
}
