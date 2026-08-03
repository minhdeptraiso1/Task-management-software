package com.project.taskmanagement.integration;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgileWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void fullAgileFlow_shouldWorkFromProjectToAssignedTask()
            throws Exception {
        testDataFactory.createUser(
                "manager",
                "manager.flow@test.com",
                UserRole.MANAGER
        );
        User developer = testDataFactory.createUser(
                "developer",
                "developer.flow@test.com",
                UserRole.EMPLOYEE
        );

        String managerToken = authTestHelper.loginAndGetAccessToken(
                mockMvc,
                "manager.flow@test.com",
                TestDataFactory.DEFAULT_PASSWORD
        );

        UUID projectId = createProject(managerToken);
        addMember(
                managerToken,
                projectId,
                developer.getId()
        );
        UUID sprintId = createSprint(managerToken, projectId);
        UUID backlogItemId = createBacklogItem(
                managerToken,
                projectId
        );
        markBacklogItemReady(
                managerToken,
                projectId,
                backlogItemId
        );
        addBacklogItemToSprint(
                managerToken,
                projectId,
                sprintId,
                backlogItemId
        );
        UUID taskId = createTask(
                managerToken,
                projectId,
                backlogItemId
        );
        assignTask(
                managerToken,
                projectId,
                taskId,
                developer.getId()
        );

        mockMvc.perform(
                        get(
                                "/projects/{projectId}/tasks/{taskId}",
                                projectId,
                                taskId
                        ).header(
                                "Authorization",
                                authTestHelper.bearer(managerToken)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(taskId.toString()))
                .andExpect(
                        jsonPath("$.data.currentSprintId")
                                .value(sprintId.toString())
                )
                .andExpect(
                        jsonPath("$.data.assigneeUserId")
                                .value(developer.getId().toString())
                )
                .andExpect(
                        jsonPath("$.data.assigneeEmail")
                                .value("developer.flow@test.com")
                );
    }

    private UUID createProject(String token) throws Exception {
        String response = mockMvc.perform(
                        post("/projects")
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                new ProjectBody(
                                                        "FLOW01",
                                                        "Flow Project",
                                                        "Full workflow test",
                                                        LocalDate.now(),
                                                        LocalDate.now().plusMonths(6)
                                                )
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractDataId(response);
    }

    private void addMember(
            String token,
            UUID projectId,
            UUID userId
    ) throws Exception {
        mockMvc.perform(
                        post("/projects/{projectId}/members", projectId)
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                new MemberBody(
                                                        userId,
                                                        "DEVELOPER"
                                                )
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(
                        jsonPath("$.data.projectRole")
                                .value("DEVELOPER")
                );
    }

    private UUID createSprint(
            String token,
            UUID projectId
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/projects/{projectId}/sprints", projectId)
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                new SprintBody(
                                                        "Sprint Integration",
                                                        "Build core flow",
                                                        LocalDate.now().plusDays(1),
                                                        LocalDate.now().plusDays(14)
                                                )
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PLANNING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractDataId(response);
    }

    private UUID createBacklogItem(
            String token,
            UUID projectId
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/projects/{projectId}/backlog-items", projectId)
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "title": "Login feature",
                                          "description": "Implement login",
                                          "type": "FEATURE",
                                          "priority": "HIGH",
                                          "storyPoints": 5
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractDataId(response);
    }

    private void markBacklogItemReady(
            String token,
            UUID projectId,
            UUID backlogItemId
    ) throws Exception {
        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/backlog-items/{itemId}/status",
                                projectId,
                                backlogItemId
                        ).header(
                                "Authorization",
                                authTestHelper.bearer(token)
                        ).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"READY\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    private void addBacklogItemToSprint(
            String token,
            UUID projectId,
            UUID sprintId,
            UUID backlogItemId
    ) throws Exception {
        mockMvc.perform(
                        post(
                                "/projects/{projectId}/sprints/{sprintId}/backlog-items/{itemId}",
                                projectId,
                                sprintId,
                                backlogItemId
                        ).header(
                                "Authorization",
                                authTestHelper.bearer(token)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.sprintId")
                                .value(sprintId.toString())
                );
    }

    private UUID createTask(
            String token,
            UUID projectId,
            UUID backlogItemId
    ) throws Exception {
        String body = """
                {
                  "backlogItemId": "%s",
                  "title": "Implement login API",
                  "description": "Create login endpoint",
                  "type": "DEVELOPMENT",
                  "priority": "HIGH",
                  "estimatedMinutes": 240,
                  "startDate": "%s",
                  "dueDate": "%s"
                }
                """.formatted(
                backlogItemId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        String response = mockMvc.perform(
                        post("/projects/{projectId}/tasks", projectId)
                                .header(
                                        "Authorization",
                                        authTestHelper.bearer(token)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractDataId(response);
    }

    private void assignTask(
            String token,
            UUID projectId,
            UUID taskId,
            UUID assigneeUserId
    ) throws Exception {
        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/assign",
                                projectId,
                                taskId
                        ).header(
                                "Authorization",
                                authTestHelper.bearer(token)
                        ).contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                new AssignBody(
                                                        assigneeUserId
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.assigneeUserId")
                                .value(assigneeUserId.toString())
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

    private record MemberBody(
            UUID userId,
            String role
    ) {
    }

    private record SprintBody(
            String name,
            String goal,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    private record AssignBody(UUID assigneeUserId) {
    }
}
