package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.bug.CreateBugCommentRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugCommentRequest;
import com.project.taskmanagement.dto.response.bug.BugCommentResponse;

import java.util.List;
import java.util.UUID;

public interface BugCommentService {

    BugCommentResponse create(UUID projectId, UUID bugId, CreateBugCommentRequest request);

    List<BugCommentResponse> getAll(UUID projectId, UUID bugId);

    BugCommentResponse update(UUID projectId, UUID bugId, UUID commentId, UpdateBugCommentRequest request);

    void delete(UUID projectId, UUID bugId, UUID commentId);
}
