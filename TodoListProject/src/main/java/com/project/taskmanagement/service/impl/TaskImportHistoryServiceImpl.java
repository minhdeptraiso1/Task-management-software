package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.imports.TaskImportHistorySearchRequest;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchDetailResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchPageResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportErrorPageResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportErrorResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.TaskImportError;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskImportErrorRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.repository.spec.TaskImportBatchSpecification;
import com.project.taskmanagement.service.TaskImportHistoryService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskImportHistoryServiceImpl
        implements TaskImportHistoryService {

    TaskImportBatchRepository taskImportBatchRepository;
    TaskImportErrorRepository taskImportErrorRepository;
    SprintRepository sprintRepository;
    UserRepository userRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    @Override
    @Transactional(readOnly = true)
    public TaskImportBatchPageResponse getImportHistory(
            UUID projectId,
            TaskImportHistorySearchRequest request,
            Pageable pageable
    ) {
        requireProjectViewAccess(projectId);
        validateDateRange(request);

        Specification<TaskImportBatch> specification =
                buildSpecification(projectId, request);

        Page<TaskImportBatchResponse> page =
                taskImportBatchRepository
                        .findAll(specification, pageable)
                        .map(this::toBatchResponse);

        return TaskImportBatchPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskImportBatchDetailResponse getImportBatchDetail(
            UUID projectId,
            UUID batchId
    ) {
        requireProjectViewAccess(projectId);

        TaskImportBatch batch =
                getBatchOrThrow(projectId, batchId);

        List<TaskImportErrorResponse> recentErrors =
                taskImportErrorRepository
                        .findTop10ByImportBatchIdOrderByRowNumberAscCreatedAtAsc(
                                batch.getId()
                        )
                        .stream()
                        .map(this::toErrorResponse)
                        .toList();

        return new TaskImportBatchDetailResponse(
                toBatchResponse(batch),
                recentErrors
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskImportErrorPageResponse getImportErrors(
            UUID projectId,
            UUID batchId,
            Pageable pageable
    ) {
        requireProjectViewAccess(projectId);

        TaskImportBatch batch =
                getBatchOrThrow(projectId, batchId);

        Page<TaskImportErrorResponse> page =
                taskImportErrorRepository
                        .findAllByImportBatchIdOrderByRowNumberAscCreatedAtAsc(
                                batch.getId(),
                                pageable
                        )
                        .map(this::toErrorResponse);

        return TaskImportErrorPageResponse.from(page);
    }

    private Specification<TaskImportBatch> buildSpecification(
            UUID projectId,
            TaskImportHistorySearchRequest request
    ) {
        Specification<TaskImportBatch> specification =
                TaskImportBatchSpecification.belongsToProject(projectId);

        if (request == null) {
            return specification;
        }

        return specification
                .and(TaskImportBatchSpecification.hasStatus(request.status()))
                .and(TaskImportBatchSpecification.hasSprintId(request.sprintId()))
                .and(TaskImportBatchSpecification.importedBy(request.importedByUserId()))
                .and(TaskImportBatchSpecification.createdFrom(request.fromDate()))
                .and(TaskImportBatchSpecification.createdTo(request.toDate()));
    }

    private void requireProjectViewAccess(UUID projectId) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(
                project,
                currentUser
        );
    }

    private TaskImportBatch getBatchOrThrow(
            UUID projectId,
            UUID batchId
    ) {
        return taskImportBatchRepository
                .findByIdAndProjectId(batchId, projectId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TASK_IMPORT_BATCH_NOT_FOUND
                        )
                );
    }

    private void validateDateRange(
            TaskImportHistorySearchRequest request
    ) {
        if (request == null
                || request.fromDate() == null
                || request.toDate() == null) {
            return;
        }

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_DATE_RANGE_INVALID
            );
        }
    }

    private TaskImportBatchResponse toBatchResponse(
            TaskImportBatch batch
    ) {
        User importer =
                batch.getImportedByUserId() == null
                        ? null
                        : userRepository
                        .findById(batch.getImportedByUserId())
                        .orElse(null);

        Sprint sprint =
                batch.getSprintId() == null
                        ? null
                        : sprintRepository
                        .findById(batch.getSprintId())
                        .orElse(null);

        return new TaskImportBatchResponse(
                batch.getId(),
                batch.getProjectId(),
                batch.getSprintId(),
                sprint == null ? null : sprint.getName(),
                batch.getOriginalFileName(),
                batch.getStatus(),
                batch.getTotalRows(),
                batch.getSuccessRows(),
                batch.getFailedRows(),
                batch.getImportedByUserId(),
                importer == null ? null : importer.getUsername(),
                importer == null ? null : importer.getEmail(),
                batch.getStartedAt(),
                batch.getCompletedAt(),
                batch.getCreatedAt()
        );
    }

    private TaskImportErrorResponse toErrorResponse(
            TaskImportError error
    ) {
        return new TaskImportErrorResponse(
                error.getId(),
                error.getImportBatchId(),
                error.getRowNumber(),
                error.getFieldName(),
                error.getRawValue(),
                error.getErrorMessage(),
                error.getCreatedAt()
        );
    }
}
