package com.project.taskmanagement.service.notification;

import com.project.taskmanagement.entity.TaskComment;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.entity.BugAttachment;
import com.project.taskmanagement.entity.BugComment;
import com.project.taskmanagement.entity.BugEvidence;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.repository.BugAttachmentRepository;
import com.project.taskmanagement.repository.BugCommentRepository;
import com.project.taskmanagement.repository.BugEvidenceRepository;
import com.project.taskmanagement.repository.TaskCommentRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class NotificationTargetUrlResolver {

    TaskCommentRepository taskCommentRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    TaskImportBatchRepository taskImportBatchRepository;
    BugCommentRepository bugCommentRepository;
    BugEvidenceRepository bugEvidenceRepository;
    BugAttachmentRepository bugAttachmentRepository;

    public String resolve(
            UUID projectId,
            ActivityEntityType entityType,
            UUID entityId
    ) {
        if (projectId == null) {
            return null;
        }

        if (entityType == null) {
            return "/projects/" + projectId;
        }

        return switch (entityType) {
            case PROJECT -> "/projects/" + projectId;

            case PROJECT_MEMBER -> "/projects/" + projectId + "/members";

            case SPRINT -> entityId == null
                    ? "/projects/" + projectId + "/sprints"
                    : "/projects/" + projectId
                      + "/sprints/" + entityId;

            case BACKLOG_ITEM -> entityId == null
                    ? "/projects/" + projectId + "/backlog"
                    : "/projects/" + projectId
                      + "/backlog-items/" + entityId;

            case TASK -> entityId == null
                    ? "/projects/" + projectId + "/tasks"
                    : "/projects/" + projectId
                      + "/tasks/" + entityId;

            case BUG -> entityId == null
                    ? "/projects/" + projectId + "/bugs"
                    : "/projects/" + projectId
                      + "/bugs/" + entityId;

            case COMMENT -> resolveCommentUrl(
                    projectId,
                    entityId
            );

            case BUG_COMMENT -> resolveBugCommentUrl(
                    projectId,
                    entityId
            );

            case BUG_EVIDENCE -> resolveBugEvidenceUrl(
                    projectId,
                    entityId
            );

            case BUG_ATTACHMENT -> resolveBugAttachmentUrl(
                    projectId,
                    entityId
            );

            case TIME_LOG -> resolveTimeLogUrl(
                    projectId,
                    entityId
            );

            case TASK_IMPORT -> resolveTaskImportUrl(
                    projectId,
                    entityId
            );
        };
    }

    // ===================== COMMENT =====================

    private String resolveCommentUrl(
            UUID projectId,
            UUID commentId
    ) {
        if (commentId == null) {
            return "/projects/" + projectId + "/tasks";
        }

        return taskCommentRepository
                .findById(commentId)
                .map(TaskComment::getTaskId)
                .map(taskId ->
                        "/projects/" + projectId
                                + "/tasks/" + taskId
                                + "?commentId=" + commentId
                )
                .orElse(
                        "/projects/" + projectId + "/tasks"
                );
    }

    // ===================== BUG =====================

    private String resolveBugCommentUrl(
            UUID projectId,
            UUID commentId
    ) {
        if (commentId == null) {
            return "/projects/" + projectId + "/bugs";
        }

        return bugCommentRepository
                .findById(commentId)
                .map(BugComment::getBugId)
                .map(bugId ->
                        "/projects/" + projectId
                                + "/bugs/" + bugId
                                + "?commentId=" + commentId
                )
                .orElse(
                        "/projects/" + projectId + "/bugs"
                );
    }

    private String resolveBugEvidenceUrl(
            UUID projectId,
            UUID evidenceId
    ) {
        if (evidenceId == null) {
            return "/projects/" + projectId + "/bugs";
        }

        return bugEvidenceRepository
                .findById(evidenceId)
                .map(BugEvidence::getBugId)
                .map(bugId ->
                        "/projects/" + projectId
                                + "/bugs/" + bugId
                                + "?tab=evidences"
                )
                .orElse(
                        "/projects/" + projectId + "/bugs"
                );
    }

    private String resolveBugAttachmentUrl(
            UUID projectId,
            UUID attachmentId
    ) {
        if (attachmentId == null) {
            return "/projects/" + projectId + "/bugs";
        }

        return bugAttachmentRepository
                .findById(attachmentId)
                .map(BugAttachment::getBugId)
                .map(bugId ->
                        "/projects/" + projectId
                                + "/bugs/" + bugId
                                + "?tab=attachments"
                )
                .orElse(
                        "/projects/" + projectId + "/bugs"
                );
    }

    // ===================== TIME LOG =====================

    private String resolveTimeLogUrl(
            UUID projectId,
            UUID timeLogId
    ) {
        if (timeLogId == null) {
            return "/projects/" + projectId + "/tasks";
        }

        return taskTimeLogRepository
                .findById(timeLogId)
                .map(TaskTimeLog::getTaskId)
                .map(taskId ->
                        "/projects/" + projectId
                                + "/tasks/" + taskId
                                + "?tab=time-logs"
                )
                .orElse(
                        "/projects/" + projectId + "/tasks"
                );
    }

    // ===================== TASK IMPORT =====================

    private String resolveTaskImportUrl(
            UUID projectId,
            UUID importBatchId
    ) {
        if (importBatchId == null) {
            return "/projects/" + projectId
                    + "/task-imports";
        }

        return taskImportBatchRepository
                .findById(importBatchId)
                .map(TaskImportBatch::getSprintId)
                .map(sprintId ->
                        "/projects/" + projectId
                                + "/sprints/" + sprintId
                                + "/task-imports/" + importBatchId
                )
                .orElse(
                        "/projects/" + projectId
                                + "/task-imports"
                );
    }
}
