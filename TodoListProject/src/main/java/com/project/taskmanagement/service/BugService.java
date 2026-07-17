package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.bug.AssignBugRequest;
import com.project.taskmanagement.dto.request.bug.BugSearchRequest;
import com.project.taskmanagement.dto.request.bug.CreateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugSeverityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugPriorityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugStatusRequest;
import com.project.taskmanagement.dto.response.bug.BugPageResponse;
import com.project.taskmanagement.dto.response.bug.BugResponse;
import com.project.taskmanagement.dto.response.bug.BugSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BugService {

    BugResponse create(
            UUID projectId,
            CreateBugRequest request
    );

    BugPageResponse search(
            UUID projectId,
            BugSearchRequest request,
            Pageable pageable
    );

    BugResponse getById(
            UUID projectId,
            UUID bugId
    );

    BugResponse update(
            UUID projectId,
            UUID bugId,
            UpdateBugRequest request
    );

    BugResponse assign(
            UUID projectId,
            UUID bugId,
            AssignBugRequest request
    );

    BugResponse unassign(
            UUID projectId,
            UUID bugId
    );

    BugResponse updateStatus(
            UUID projectId,
            UUID bugId,
            UpdateBugStatusRequest request
    );

    BugResponse updateSeverity(
            UUID projectId,
            UUID bugId,
            UpdateBugSeverityRequest request
    );

    BugResponse updatePriority(
            UUID projectId,
            UUID bugId,
            UpdateBugPriorityRequest request
    );

    void delete(
            UUID projectId,
            UUID bugId
    );

    BugSummaryResponse getSummary(
            UUID projectId
    );
}
