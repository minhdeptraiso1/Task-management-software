package com.project.taskmanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectTaskSecurityIT extends BaseSecurityIT {

    @Test
    void projectMemberAndAdmin_canViewProject_butOutsiderCannot() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();

        mockMvc.perform(
                        get("/projects/{projectId}", fixture.project().getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/projects/{projectId}", fixture.project().getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                )
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/projects/{projectId}", fixture.project().getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void assignedEmployee_canMoveOwnTaskStatus() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/status",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"IN_PROGRESS\",\"position\":1}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void regularProjectMember_canAssignTaskToAnotherMember() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/assign",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"assigneeUserId\":\"" + otherMember.getId() + "\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assigneeUserId")
                        .value(otherMember.getId().toString()));
    }

    @Test
    void outsider_cannotReadOrMoveProjectTask() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();

        mockMvc.perform(
                        get(
                                "/projects/{projectId}/tasks/{taskId}",
                                fixture.project().getId(),
                                fixture.task().getId()
                        ).header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/status",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"IN_PROGRESS\",\"position\":1}")
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/assign",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"assigneeUserId\":\"" + otherMember.getId() + "\"}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_isViewOnlyAndCannotCreateOrAssignProjectTask() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();

        mockMvc.perform(
                        post("/projects/{projectId}/tasks", fixture.project().getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createTaskBody(fixture))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/assign",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"assigneeUserId\":\"" + otherMember.getId() + "\"}")
                )
                .andExpect(status().isForbidden());
    }

    private String createTaskBody(ProjectTaskFixture fixture) {
        return """
                {
                  "backlogItemId": "%s",
                  "title": "Admin must not create this task",
                  "description": "Security regression",
                  "type": "DEVELOPMENT",
                  "priority": "MEDIUM",
                  "estimatedMinutes": 60,
                  "startDate": "%s",
                  "dueDate": "%s"
                }
                """.formatted(
                fixture.backlogItem().getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );
    }
}
