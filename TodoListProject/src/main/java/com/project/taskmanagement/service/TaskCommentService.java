package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.taskcomment.CreateTaskCommentRequest;
import com.project.taskmanagement.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentPageResponse;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentReplyResponse;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskCommentService {

    TaskCommentResponse create(
            UUID projectId,
            UUID taskId,
            CreateTaskCommentRequest request
    );

    TaskCommentPageResponse getComments(
            UUID projectId,
            UUID taskId,
            Pageable pageable
    );

    TaskCommentResponse update(
            UUID projectId,
            UUID taskId,
            UUID commentId,
            UpdateTaskCommentRequest request
    );

    void delete(
            UUID projectId,
            UUID taskId,
            UUID commentId
    );

    List<TaskCommentReplyResponse> getReplies(
            UUID projectId,
            UUID taskId,
            UUID commentId
    );

    TaskCommentReplyResponse createReply(
            UUID projectId,
            UUID taskId,
            UUID commentId,
            CreateTaskCommentRequest request
    );
}
