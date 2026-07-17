package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.bug.CreateBugEvidenceRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugEvidenceRequest;
import com.project.taskmanagement.dto.response.bug.BugEvidenceResponse;

import java.util.List;
import java.util.UUID;

public interface BugEvidenceService {

    BugEvidenceResponse create(UUID projectId, UUID bugId, CreateBugEvidenceRequest request);

    List<BugEvidenceResponse> getAll(UUID projectId, UUID bugId);

    BugEvidenceResponse update(UUID projectId, UUID bugId, UUID evidenceId, UpdateBugEvidenceRequest request);

    void delete(UUID projectId, UUID bugId, UUID evidenceId);
}
