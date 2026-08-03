package com.project.taskmanagement.service.notification;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.repository.AttachmentRepository;
import com.project.taskmanagement.repository.BugAttachmentRepository;
import com.project.taskmanagement.repository.BugCommentRepository;
import com.project.taskmanagement.repository.BugEvidenceRepository;
import com.project.taskmanagement.repository.TaskCommentRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationTargetUrlResolverTest {

    @Mock TaskCommentRepository taskCommentRepository;
    @Mock TaskTimeLogRepository taskTimeLogRepository;
    @Mock TaskImportBatchRepository taskImportBatchRepository;
    @Mock BugCommentRepository bugCommentRepository;
    @Mock BugEvidenceRepository bugEvidenceRepository;
    @Mock BugAttachmentRepository bugAttachmentRepository;
    @Mock AttachmentRepository attachmentRepository;

    @InjectMocks
    NotificationTargetUrlResolver resolver;

    @Test
    void resolvesDirectProjectTaskSprintAndBugUrls() {
        UUID projectId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        assertThat(resolver.resolve(
                projectId,
                ActivityEntityType.PROJECT,
                entityId
        )).isEqualTo("/projects/" + projectId);
        assertThat(resolver.resolve(
                projectId,
                ActivityEntityType.TASK,
                entityId
        )).isEqualTo("/projects/" + projectId + "/tasks/" + entityId);
        assertThat(resolver.resolve(
                projectId,
                ActivityEntityType.SPRINT,
                entityId
        )).isEqualTo("/projects/" + projectId + "/sprints/" + entityId);
        assertThat(resolver.resolve(
                projectId,
                ActivityEntityType.BUG,
                entityId
        )).isEqualTo("/projects/" + projectId + "/bugs/" + entityId);
    }

    @Test
    void fallsBackToCollectionOrProjectUrlWhenTargetIsIncomplete() {
        UUID projectId = UUID.randomUUID();

        assertThat(resolver.resolve(null, ActivityEntityType.TASK, UUID.randomUUID()))
                .isNull();
        assertThat(resolver.resolve(projectId, null, UUID.randomUUID()))
                .isEqualTo("/projects/" + projectId);
        assertThat(resolver.resolve(projectId, ActivityEntityType.TASK, null))
                .isEqualTo("/projects/" + projectId + "/tasks");
        assertThat(resolver.resolve(projectId, ActivityEntityType.COMMENT, UUID.randomUUID()))
                .isEqualTo("/projects/" + projectId + "/tasks");
    }
}
