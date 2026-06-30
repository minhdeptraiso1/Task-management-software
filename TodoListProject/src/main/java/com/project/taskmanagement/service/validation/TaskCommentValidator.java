package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.TaskComment;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

public final class TaskCommentValidator {

    private TaskCommentValidator() {
    }

    public static String normalizeContent(
            String content
    ) {
        if (content == null
                || content.trim().isBlank()) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_CONTENT_INVALID
            );
        }

        String normalized =
                content.trim();

        if (normalized.length() > 5000) {
            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_CONTENT_INVALID
            );
        }

        return normalized;
    }

    /**
     * Chỉ hỗ trợ:
     * <p>
     * Comment gốc
     * └── Reply
     * <p>
     * Không hỗ trợ reply của reply.
     */
    public static void validateParentDepth(
            TaskComment parentComment
    ) {
        if (parentComment.getParentCommentId()
                != null) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_COMMENT_REPLY_DEPTH_INVALID
            );
        }
    }
}