package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.backlog.*;
import com.project.taskmanagement.dto.response.backlog.BacklogItemPageResponse;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.BacklogItemMapper;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.spec.BacklogItemSpecification;
import com.project.taskmanagement.service.BacklogItemService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.cache.CacheEvictService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.BacklogItemValidator;
import com.project.taskmanagement.service.validation.SprintValidator;
import com.project.taskmanagement.util.TextNormalizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class BacklogItemServiceImpl
        implements BacklogItemService {

    BacklogItemRepository backlogItemRepository;
    BacklogItemMapper backlogItemMapper;

    ProjectAccessService projectAccessService;
    CurrentUserService currentUserService;
    ProjectActivityService projectActivityService;
    CacheEvictService cacheEvictService;

    SprintRepository sprintRepository;
    // ===================== CREATE =====================

    @Override
    @Transactional
    public BacklogItemResponse create(
            UUID projectId,
            CreateBacklogItemRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(project);

        Long maxPosition =
                backlogItemRepository
                        .findMaxBacklogPosition(
                                projectId
                        );

        long nextPosition =
                maxPosition == null
                        ? 1L
                        : maxPosition + 1L;

        BacklogPriority priority =
                request.priority() != null
                        ? request.priority()
                        : BacklogPriority.MEDIUM;

        BacklogItem backlogItem =
                BacklogItem.builder()
                        .projectId(projectId)
                        .sprintId(null)
                        .title(
                                TextNormalizer.trim(
                                        request.title()
                                )
                        )
                        .description(
                                TextNormalizer.trimToNull(
                                        request.description()
                                )
                        )
                        .type(request.type())
                        .status(
                                BacklogItemStatus.DRAFT
                        )
                        .priority(priority)
                        .storyPoints(
                                request.storyPoints()
                        )
                        .position(nextPosition)
                        .createdByUserId(
                                currentUser.getId()
                        )
                        .build();

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "title",
                savedItem.getTitle()
        );
        newValue.put(
                "type",
                savedItem.getType()
        );
        newValue.put(
                "priority",
                savedItem.getPriority()
        );
        newValue.put(
                "status",
                savedItem.getStatus()
        );
        newValue.put(
                "storyPoints",
                savedItem.getStoryPoints()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_CREATED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        evictBacklogCache(savedItem);
        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    // ===================== SEARCH =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.BACKLOG_ITEM_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword())" +
                    " + '|status=' + (#request == null || #request.status() == null ? '' : #request.status())" +
                    " + '|type=' + (#request == null || #request.type() == null ? '' : #request.type())" +
                    " + '|priority=' + (#request == null || #request.priority() == null ? '' : #request.priority())" +
                    " + '|sprint=' + (#request == null || #request.sprintId() == null ? '' : #request.sprintId())" +
                    " + '|unscheduled=' + (#request == null || #request.unscheduledOnly() == null ? '' : #request.unscheduledOnly())" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public BacklogItemPageResponse search(
            UUID projectId,
            BacklogSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        String keyword =
                request != null
                        ? request.keyword()
                        : null;

        BacklogItemStatus status =
                request != null
                        ? request.status()
                        : null;

        var type =
                request != null
                        ? request.type()
                        : null;

        var priority =
                request != null
                        ? request.priority()
                        : null;

        UUID sprintId =
                request != null
                        ? request.sprintId()
                        : null;

        Boolean unscheduledOnly =
                request != null
                        ? request.unscheduledOnly()
                        : null;

        Specification<BacklogItem> specification =
                Specification.allOf(
                        BacklogItemSpecification
                                .belongsToProject(projectId),
                        BacklogItemSpecification
                                .search(keyword),
                        BacklogItemSpecification
                                .hasStatus(status),
                        BacklogItemSpecification
                                .hasType(type),
                        BacklogItemSpecification
                                .hasPriority(priority),
                        BacklogItemSpecification
                                .belongsToSprint(sprintId),
                        BacklogItemSpecification
                                .unscheduledOnly(
                                        unscheduledOnly
                                )
                );

        Page<BacklogItemResponse> responsePage =
                backlogItemRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(
                                backlogItemMapper::toResponse
                        );

        return BacklogItemPageResponse.from(
                responsePage
        );
    }

    // ===================== DETAIL =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.BACKLOG_ITEM_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #itemId"
    )
    public BacklogItemResponse getById(
            UUID projectId,
            UUID itemId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        return backlogItemMapper.toResponse(
                backlogItem
        );
    }

    // ===================== UPDATE =====================

    @Override
    @Transactional
    public BacklogItemResponse update(
            UUID projectId,
            UUID itemId,
            UpdateBacklogItemRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(project);

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        BacklogItemValidator.validateEditable(
                backlogItem
        );

        Map<String, Object> oldValue =
                backlogSnapshot(backlogItem);

        if (request.title() != null) {
            String normalizedTitle =
                    TextNormalizer.trim(
                            request.title()
                    );

            if (normalizedTitle.isBlank()) {
                throw new BusinessException(
                        ErrorCode.BACKLOG_ITEM_TITLE_REQUIRED
                );
            }

            backlogItem.setTitle(
                    normalizedTitle
            );
        }

        if (request.description() != null) {
            backlogItem.setDescription(
                    TextNormalizer.trimToNull(
                            request.description()
                    )
            );
        }

        if (request.type() != null) {
            backlogItem.setType(
                    request.type()
            );
        }

        if (request.storyPoints() != null) {
            backlogItem.setStoryPoints(
                    request.storyPoints()
            );
        }

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        backlogSnapshot(savedItem)
                )
        );

        evictBacklogCache(savedItem);
        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    // ===================== UPDATE STATUS =====================

    @Override
    @Transactional
    public BacklogItemResponse updateStatus(
            UUID projectId,
            UUID itemId,
            UpdateBacklogItemStatusRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(
                        project
                );

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        BacklogItemStatus oldStatus =
                backlogItem.getStatus();

        BacklogItemStatus newStatus =
                request.status();

        if (oldStatus == newStatus) {
            return backlogItemMapper.toResponse(
                    backlogItem
            );
        }

        /*
         * Không cho API status tự đưa item vào Sprint.
         * Việc đó phải đi qua API addBacklogItem.
         */
        if (newStatus
                == BacklogItemStatus.IN_SPRINT) {

            throw new BusinessException(
                    ErrorCode
                            .BACKLOG_ITEM_STATUS_TRANSITION_INVALID
            );
        }

        /*
         * Item đang trong Sprint chỉ được chuyển sang DONE.
         */
        if (oldStatus
                == BacklogItemStatus.IN_SPRINT) {

            if (newStatus
                    != BacklogItemStatus.DONE) {

                throw new BusinessException(
                        ErrorCode
                                .BACKLOG_ITEM_STATUS_TRANSITION_INVALID
                );
            }

            UUID sprintId =
                    backlogItem.getSprintId();

            if (sprintId == null) {
                throw new BusinessException(
                        ErrorCode.BACKLOG_ITEM_DONE_INVALID
                );
            }

            Sprint sprint =
                    sprintRepository
                            .findByIdAndProjectId(
                                    sprintId,
                                    projectId
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode.SPRINT_NOT_FOUND
                                    )
                            );

            if (sprint.getStatus()
                    != SprintStatus.ACTIVE) {

                throw new BusinessException(
                        ErrorCode.BACKLOG_ITEM_DONE_INVALID
                );
            }

        } else {
            /*
             * Với item ngoài Sprint, dùng flow trạng thái cũ.
             */
            BacklogItemValidator
                    .validateStatusTransition(
                            oldStatus,
                            newStatus
                    );
        }

        backlogItem.setStatus(
                newStatus
        );

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_STATUS_CHANGED,
                        currentUser.getId(),
                        Map.of(
                                "status",
                                oldStatus
                        ),
                        Map.of(
                                "status",
                                newStatus
                        )
                )
        );

        evictBacklogCache(savedItem);
        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    // ===================== UPDATE PRIORITY =====================

    @Override
    @Transactional
    public BacklogItemResponse updatePriority(
            UUID projectId,
            UUID itemId,
            UpdateBacklogPriorityRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(project);

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        BacklogItemValidator.validateEditable(
                backlogItem
        );

        BacklogPriority oldPriority =
                backlogItem.getPriority();

        BacklogPriority newPriority =
                request.priority();

        backlogItem.setPriority(
                newPriority
        );

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_PRIORITY_CHANGED,
                        currentUser.getId(),
                        Map.of(
                                "priority",
                                oldPriority
                        ),
                        Map.of(
                                "priority",
                                newPriority
                        )
                )
        );

        evictBacklogCache(savedItem);
        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    // ===================== DELETE =====================

    @Override
    @Transactional
    public void delete(
            UUID projectId,
            UUID itemId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(project);

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        BacklogItemValidator
                .validateNotInSprint(
                        backlogItem
                );

        Map<String, Object> oldValue =
                backlogSnapshot(backlogItem);

        backlogItem.markDeleted(
                currentUser.getUsername()
        );

        backlogItemRepository.save(
                backlogItem
        );

        evictBacklogCache(backlogItem);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        itemId,
                        ProjectActivityAction
                                .BACKLOG_ITEM_DELETED,
                        currentUser.getId(),
                        oldValue,
                        null
                )
        );
    }

    // ===================== method reorder =====================
    @Override
    @Transactional
    public BacklogItemResponse updatePosition(
            UUID projectId,
            UUID itemId,
            UpdateBacklogPositionRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        BacklogItemValidator
                .validateProjectEditable(project);

        BacklogItem backlogItem =
                getItemOrThrow(
                        projectId,
                        itemId
                );

        UUID sprintId =
                backlogItem.getSprintId();

        if (sprintId != null) {
            Sprint sprint =
                    sprintRepository
                            .findByIdAndProjectId(
                                    sprintId,
                                    projectId
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode.SPRINT_NOT_FOUND
                                    )
                            );

            SprintValidator.validatePlanning(
                    sprint
            );
        }

        Long oldPosition =
                backlogItem.getPosition();

        Long requestedPosition =
                request.position();

        long itemCount;

        if (sprintId == null) {
            itemCount =
                    backlogItemRepository
                            .findAllByProjectIdAndSprintIdIsNullOrderByPositionAsc(
                                    projectId
                            )
                            .size();
        } else {
            itemCount =
                    backlogItemRepository
                            .countByProjectIdAndSprintId(
                                    projectId,
                                    sprintId
                            );
        }

        long newPosition =
                Math.min(
                        requestedPosition,
                        Math.max(itemCount, 1L)
                );

        if (oldPosition.equals(newPosition)) {
            return backlogItemMapper.toResponse(
                    backlogItem
            );
        }

        if (newPosition < oldPosition) {
            backlogItemRepository.moveRangeDown(
                    projectId,
                    sprintId,
                    itemId,
                    newPosition,
                    oldPosition
            );
        } else {
            backlogItemRepository.moveRangeUp(
                    projectId,
                    sprintId,
                    itemId,
                    oldPosition,
                    newPosition
            );
        }

        backlogItem.setPosition(
                newPosition
        );

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "position",
                oldPosition
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "position",
                newPosition
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        itemId,
                        ProjectActivityAction
                                .BACKLOG_ITEM_POSITION_CHANGED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        evictBacklogCache(savedItem);
        return backlogItemMapper.toResponse(
                savedItem
        );
    }
    // ===================== HELPER =====================

    private void evictBacklogCache(BacklogItem backlogItem) {
        cacheEvictService.evictBacklogWorkspace(
                backlogItem.getProjectId(),
                backlogItem.getSprintId()
        );
    }

    private BacklogItem getItemOrThrow(
            UUID projectId,
            UUID itemId
    ) {
        return backlogItemRepository
                .findByIdAndProjectId(
                        itemId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .BACKLOG_ITEM_NOT_FOUND
                        )
                );
    }

    private Map<String, Object> backlogSnapshot(
            BacklogItem backlogItem
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "title",
                backlogItem.getTitle()
        );
        value.put(
                "description",
                backlogItem.getDescription()
        );
        value.put(
                "type",
                backlogItem.getType()
        );
        value.put(
                "status",
                backlogItem.getStatus()
        );
        value.put(
                "priority",
                backlogItem.getPriority()
        );
        value.put(
                "storyPoints",
                backlogItem.getStoryPoints()
        );
        value.put(
                "sprintId",
                backlogItem.getSprintId()
        );
        value.put(
                "position",
                backlogItem.getPosition()
        );

        return value;
    }
}
