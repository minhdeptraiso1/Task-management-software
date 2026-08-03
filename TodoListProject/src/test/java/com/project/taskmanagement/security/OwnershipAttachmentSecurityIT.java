package com.project.taskmanagement.security;

import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.entity.TaskComment;
import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.repository.TaskCommentRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OwnershipAttachmentSecurityIT extends BaseSecurityIT {

    @Autowired
    private TaskCommentRepository commentRepository;
    @Autowired
    private TaskTimeLogRepository timeLogRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationRecipientRepository recipientRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void commentOwnerCanUpdate_butAnotherProjectMemberCannot() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        TaskComment comment = createComment(fixture);

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                                fixture.project().getId(),
                                fixture.task().getId(),
                                comment.getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(otherMemberToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"Illegal update\"}")
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                                fixture.project().getId(),
                                fixture.task().getId(),
                                comment.getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"Owner update\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Owner update"));
    }

    @Test
    void timeLogOwnerCanUpdate_butAnotherProjectMemberCannot() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        TaskTimeLog timeLog = createTimeLog(fixture);

        String body = "{\"minutes\":90,\"description\":\"Security update\"}";
        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/time-logs/{timeLogId}",
                                fixture.project().getId(),
                                fixture.task().getId(),
                                timeLog.getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(otherMemberToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch(
                                "/projects/{projectId}/tasks/{taskId}/time-logs/{timeLogId}",
                                fixture.project().getId(),
                                fixture.task().getId(),
                                timeLog.getId()
                        )
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.minutes").value(90));
    }

    @Test
    void notificationOfAnotherUser_isHiddenAsNotFound() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        Notification notification = createNotificationForEmployee(fixture);

        mockMvc.perform(
                        patch("/notifications/{notificationId}/read", notification.getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(otherMemberToken))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void markAllRead_onlyChangesCurrentUsersRecipients() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        Notification employeeNotification = createNotificationForEmployee(fixture);
        Notification otherNotification = notificationRepository.saveAndFlush(
                TestEntityFactory.notification(
                        manager.getId(),
                        fixture.project().getId(),
                        fixture.task().getId()
                )
        );
        NotificationRecipient otherRecipient = recipientRepository.saveAndFlush(
                TestEntityFactory.recipient(otherNotification.getId(), otherMember.getId(), null)
        );

        mockMvc.perform(
                        patch("/notifications/read-all")
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isNoContent());

        entityManager.clear();
        assertThat(recipientRepository.findByNotificationIdAndUserId(
                employeeNotification.getId(), employee.getId()
        ).orElseThrow().getReadAt()).isNotNull();
        assertThat(recipientRepository.findById(otherRecipient.getId()).orElseThrow().getReadAt()).isNull();
    }

    @Test
    void dangerousAndPathTraversalFileNames_areRejected() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        MockMultipartFile dangerous = new MockMultipartFile(
                "file",
                "virus.exe",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "danger".getBytes()
        );
        MockMultipartFile traversal = new MockMultipartFile(
                "file",
                "../hack.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hack".getBytes()
        );

        mockMvc.perform(
                        multipart(
                                "/projects/{projectId}/attachments/TASK/{taskId}",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .file(dangerous)
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        multipart(
                                "/projects/{projectId}/attachments/TASK/{taskId}",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .file(traversal)
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void outsiderCannotListUploadOrDownloadProjectAttachments() throws Exception {
        ProjectTaskFixture fixture = createProjectTaskFixture();
        MockMultipartFile safeText = new MockMultipartFile(
                "file",
                "note.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        mockMvc.perform(
                        get("/projects/{projectId}/attachments", fixture.project().getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        multipart(
                                "/projects/{projectId}/attachments/TASK/{taskId}",
                                fixture.project().getId(),
                                fixture.task().getId()
                        )
                                .file(safeText)
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get(
                                "/projects/{projectId}/attachments/{attachmentId}/download",
                                fixture.project().getId(),
                                java.util.UUID.randomUUID()
                        ).header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isForbidden());
    }

    private TaskComment createComment(ProjectTaskFixture fixture) {
        authenticateForAuditing(employee);
        TaskComment comment = commentRepository.saveAndFlush(
                TaskComment.builder()
                        .taskId(fixture.task().getId())
                        .userId(employee.getId())
                        .content("Employee comment")
                        .build()
        );
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return comment;
    }

    private TaskTimeLog createTimeLog(ProjectTaskFixture fixture) {
        authenticateForAuditing(employee);
        TaskTimeLog timeLog = timeLogRepository.saveAndFlush(
                TestEntityFactory.timeLog(
                        fixture.task().getId(),
                        employee.getId(),
                        LocalDate.now(),
                        60
                )
        );
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return timeLog;
    }

    private Notification createNotificationForEmployee(ProjectTaskFixture fixture) {
        authenticateForAuditing(manager);
        Notification notification = notificationRepository.saveAndFlush(
                TestEntityFactory.notification(
                        manager.getId(),
                        fixture.project().getId(),
                        fixture.task().getId()
                )
        );
        recipientRepository.saveAndFlush(
                TestEntityFactory.recipient(notification.getId(), employee.getId(), null)
        );
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return notification;
    }
}
