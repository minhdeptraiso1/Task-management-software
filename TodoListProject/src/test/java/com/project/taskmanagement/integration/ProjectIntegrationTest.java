package com.project.taskmanagement.integration;

import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

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
                .andExpect(jsonPath("$.success").value(true))
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
                .andExpect(jsonPath("$.success").value(false));
    }

    private String projectBody(
            String code,
            String name
    ) throws Exception {
        return json(
                new ProjectBody(
                        code,
                        name,
                        "Project integration test",
                        LocalDate.now(),
                        LocalDate.now().plusMonths(6)
                )
        );
    }

    private record ProjectBody(
            String code,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
