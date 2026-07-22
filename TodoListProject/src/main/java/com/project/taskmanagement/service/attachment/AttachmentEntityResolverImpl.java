package com.project.taskmanagement.service.attachment;

import com.project.taskmanagement.entity.BugComment;
import com.project.taskmanagement.entity.BugEvidence;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskComment;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugCommentRepository;
import com.project.taskmanagement.repository.BugEvidenceRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.TaskCommentRepository;
import com.project.taskmanagement.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentEntityResolverImpl implements AttachmentEntityResolver {

    TaskRepository taskRepository;
    TaskCommentRepository taskCommentRepository;
    BugRepository bugRepository;
    BugCommentRepository bugCommentRepository;
    BugEvidenceRepository bugEvidenceRepository;

    @Override
    public void validateEntityExists(UUID projectId, AttachmentEntityType entityType, UUID entityId) {
        switch (entityType) {
            case TASK -> validateTask(projectId, entityId);
            case COMMENT -> validateTaskComment(projectId, entityId);
            case BUG -> validateBug(projectId, entityId);
            case BUG_COMMENT -> validateBugComment(projectId, entityId);
            case BUG_EVIDENCE -> validateBugEvidence(projectId, entityId);
            default -> throw new BusinessException(ErrorCode.FILE_ENTITY_TYPE_NOT_SUPPORTED);
        }
    }

    private void validateTask(UUID projectId, UUID taskId) {
        taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
    }

    private void validateTaskComment(UUID projectId, UUID commentId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_COMMENT_NOT_FOUND));

        Task task = taskRepository.findById(comment.getTaskId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        if (!projectId.equals(task.getProjectId())) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_NOT_FOUND);
        }
    }

    private void validateBug(UUID projectId, UUID bugId) {
        bugRepository.findByIdAndProjectId(bugId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_NOT_FOUND));
    }

    private void validateBugComment(UUID projectId, UUID commentId) {
        BugComment comment = bugCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_COMMENT_NOT_FOUND));

        if (!projectId.equals(comment.getProjectId())) {
            throw new BusinessException(ErrorCode.BUG_COMMENT_NOT_FOUND);
        }
    }

    private void validateBugEvidence(UUID projectId, UUID entityId) {
        boolean isEvidenceId = bugEvidenceRepository.findById(entityId)
                .map(BugEvidence::getProjectId)
                .filter(projectId::equals)
                .isPresent();

        if (isEvidenceId) {
            return;
        }

        bugRepository.findByIdAndProjectId(entityId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_EVIDENCE_NOT_FOUND));
    }
}
