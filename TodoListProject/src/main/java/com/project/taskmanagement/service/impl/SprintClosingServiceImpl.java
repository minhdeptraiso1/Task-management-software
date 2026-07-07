package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.sprint.SprintRetroActionItemRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintRetrospectiveRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintReviewRequest;
import com.project.taskmanagement.dto.response.sprint.SprintClosingReportResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRetroActionItemResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRetrospectiveResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReviewResponse;
import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.SprintRetrospective;
import com.project.taskmanagement.entity.SprintReview;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.SprintRetrospectiveRepository;
import com.project.taskmanagement.repository.SprintReviewRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SprintClosingService;
import com.project.taskmanagement.service.TaskStatisticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.util.TextNormalizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintClosingServiceImpl
        implements SprintClosingService {

    SprintRepository sprintRepository;
    SprintReviewRepository sprintReviewRepository;
    SprintRetrospectiveRepository sprintRetrospectiveRepository;
    BacklogItemRepository backlogItemRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    TaskStatisticsService taskStatisticsService;

    ObjectMapper objectMapper;

    // ===================== CLOSING REPORT =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_CLOSING_REPORT,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintClosingReportResponse getClosingReport(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(projectId);

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        List<BacklogItem> backlogItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        long completedBacklogItemCount =
                backlogItems.stream()
                        .filter(item ->
                                item.getStatus()
                                        == BacklogItemStatus.DONE
                        )
                        .count();

        long totalStoryPoints =
                backlogItems.stream()
                        .map(BacklogItem::getStoryPoints)
                        .filter(points -> points != null)
                        .mapToLong(Integer::longValue)
                        .sum();

        SprintReviewResponse review =
                sprintReviewRepository
                        .findByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        )
                        .map(this::toReviewResponse)
                        .orElse(null);

        SprintRetrospectiveResponse retrospective =
                sprintRetrospectiveRepository
                        .findByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        )
                        .map(this::toRetrospectiveResponse)
                        .orElse(null);

        return new SprintClosingReportResponse(
                projectId,
                sprintId,
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStartedAt(),
                sprint.getCompletedAt(),
                backlogItems.size(),
                completedBacklogItemCount,
                backlogItems.size()
                        - completedBacklogItemCount,
                totalStoryPoints,
                taskStatisticsService
                        .getSprintStatistics(
                                projectId,
                                sprintId
                        ),
                taskStatisticsService
                        .getSprintBurndown(
                                projectId,
                                sprintId
                        ),
                review,
                retrospective,
                Instant.now()
        );
    }

    // ===================== REVIEW =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_REVIEW,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintReviewResponse getReview(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(projectId);
        getSprintOrThrow(projectId, sprintId);

        return sprintReviewRepository
                .findByProjectIdAndSprintId(
                        projectId,
                        sprintId
                )
                .map(this::toReviewResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SPRINT_REVIEW, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CLOSING_REPORT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_DETAIL, allEntries = true)
    })
    public SprintReviewResponse updateReview(
            UUID projectId,
            UUID sprintId,
            UpdateSprintReviewRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireProjectUpdateAccess(
                        projectId,
                        currentUser
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintReview review =
                sprintReviewRepository
                        .findByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        )
                        .orElseGet(() ->
                                SprintReview.builder()
                                        .projectId(projectId)
                                        .sprintId(sprintId)
                                        .build()
                        );

        Map<String, Object> oldValue =
                reviewSnapshot(review);

        review.setGoalAchieved(
                request.goalAchieved()
        );

        review.setDemoSummary(
                TextNormalizer.trimToNull(
                        request.demoSummary()
                )
        );

        review.setStakeholderFeedback(
                TextNormalizer.trimToNull(
                        request.stakeholderFeedback()
                )
        );

        review.setAcceptedItemSummary(
                TextNormalizer.trimToNull(
                        request.acceptedItemSummary()
                )
        );

        review.setRejectedItemSummary(
                TextNormalizer.trimToNull(
                        request.rejectedItemSummary()
                )
        );

        review.setNote(
                TextNormalizer.trimToNull(
                        request.note()
                )
        );

        SprintReview savedReview =
                sprintReviewRepository.save(
                        review
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprint.getId(),
                        ProjectActivityAction.SPRINT_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        reviewSnapshot(savedReview)
                )
        );

        return toReviewResponse(
                savedReview
        );
    }

    // ===================== RETROSPECTIVE =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_RETROSPECTIVE,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintRetrospectiveResponse getRetrospective(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(projectId);
        getSprintOrThrow(projectId, sprintId);

        return sprintRetrospectiveRepository
                .findByProjectIdAndSprintId(
                        projectId,
                        sprintId
                )
                .map(this::toRetrospectiveResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SPRINT_RETROSPECTIVE, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CLOSING_REPORT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_DETAIL, allEntries = true)
    })
    public SprintRetrospectiveResponse updateRetrospective(
            UUID projectId,
            UUID sprintId,
            UpdateSprintRetrospectiveRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireProjectUpdateAccess(
                        projectId,
                        currentUser
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        validateActionItems(
                projectId,
                request.actionItems()
        );

        SprintRetrospective retrospective =
                sprintRetrospectiveRepository
                        .findByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        )
                        .orElseGet(() ->
                                SprintRetrospective.builder()
                                        .projectId(projectId)
                                        .sprintId(sprintId)
                                        .build()
                        );

        Map<String, Object> oldValue =
                retrospectiveSnapshot(retrospective);

        retrospective.setWentWell(
                TextNormalizer.trimToNull(
                        request.wentWell()
                )
        );

        retrospective.setWentWrong(
                TextNormalizer.trimToNull(
                        request.wentWrong()
                )
        );

        retrospective.setImprovement(
                TextNormalizer.trimToNull(
                        request.improvement()
                )
        );

        retrospective.setActionItemsJson(
                serializeActionItems(
                        request.actionItems()
                )
        );

        retrospective.setNote(
                TextNormalizer.trimToNull(
                        request.note()
                )
        );

        SprintRetrospective savedRetrospective =
                sprintRetrospectiveRepository.save(
                        retrospective
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprint.getId(),
                        ProjectActivityAction.SPRINT_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        retrospectiveSnapshot(savedRetrospective)
                )
        );

        return toRetrospectiveResponse(
                savedRetrospective
        );
    }

    // ===================== VALIDATION =====================

    private void requireViewAccess(
            UUID projectId
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
    }

    private Sprint getSprintOrThrow(
            UUID projectId,
            UUID sprintId
    ) {
        return sprintRepository
                .findByIdAndProjectId(
                        sprintId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SPRINT_NOT_FOUND
                        )
                );
    }

    private void validateActionItems(
            UUID projectId,
            List<SprintRetroActionItemRequest> actionItems
    ) {
        if (actionItems == null
                || actionItems.isEmpty()) {

            return;
        }

        for (SprintRetroActionItemRequest item : actionItems) {
            if (item.assigneeUserId() == null) {
                continue;
            }

            boolean memberExists =
                    projectMemberRepository
                            .findByProjectIdAndUserId(
                                    projectId,
                                    item.assigneeUserId()
                            )
                            .isPresent();

            if (!memberExists) {
                throw new BusinessException(
                        ErrorCode.PROJECT_MEMBER_NOT_FOUND
                );
            }
        }
    }

    // ===================== MAPPER =====================

    private SprintReviewResponse toReviewResponse(
            SprintReview review
    ) {
        return new SprintReviewResponse(
                review.getId(),
                review.getProjectId(),
                review.getSprintId(),
                review.getGoalAchieved(),
                review.getDemoSummary(),
                review.getStakeholderFeedback(),
                review.getAcceptedItemSummary(),
                review.getRejectedItemSummary(),
                review.getNote(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private SprintRetrospectiveResponse toRetrospectiveResponse(
            SprintRetrospective retrospective
    ) {
        List<SprintRetroActionItemResponse> actionItems =
                parseActionItems(
                        retrospective.getActionItemsJson()
                );

        return new SprintRetrospectiveResponse(
                retrospective.getId(),
                retrospective.getProjectId(),
                retrospective.getSprintId(),
                retrospective.getWentWell(),
                retrospective.getWentWrong(),
                retrospective.getImprovement(),
                actionItems,
                retrospective.getNote(),
                retrospective.getCreatedAt(),
                retrospective.getUpdatedAt()
        );
    }

    private List<SprintRetroActionItemResponse> parseActionItems(
            String json
    ) {
        if (json == null
                || json.isBlank()) {

            return List.of();
        }

        try {
            List<SprintRetroActionItemRequest> requests =
                    objectMapper.readValue(
                            json,
                            new TypeReference<List<SprintRetroActionItemRequest>>() {
                            }
                    );

            List<UUID> userIds =
                    requests.stream()
                            .map(SprintRetroActionItemRequest::assigneeUserId)
                            .filter(id -> id != null)
                            .distinct()
                            .toList();

            Map<UUID, User> usersById =
                    userRepository
                            .findAllById(userIds)
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            User::getId,
                                            Function.identity(),
                                            (left, right) -> left,
                                            LinkedHashMap::new
                                    )
                            );

            return requests.stream()
                    .map(item -> {
                        User user =
                                item.assigneeUserId() == null
                                        ? null
                                        : usersById.get(
                                        item.assigneeUserId()
                                );

                        return new SprintRetroActionItemResponse(
                                item.content(),
                                item.assigneeUserId(),
                                user != null
                                        ? user.getUsername()
                                        : null,
                                user != null
                                        ? user.getEmail()
                                        : null,
                                item.dueDate(),
                                Boolean.TRUE.equals(
                                        item.done()
                                )
                        );
                    })
                    .toList();

        } catch (Exception exception) {
            return List.of();
        }
    }

    // ===================== JSON =====================

    private String serializeActionItems(
            List<SprintRetroActionItemRequest> actionItems
    ) {
        if (actionItems == null
                || actionItems.isEmpty()) {

            return null;
        }

        try {
            return objectMapper
                    .writeValueAsString(
                            actionItems
                    );

        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.JSON_PROCESSING_ERROR
            );
        }
    }

    // ===================== SNAPSHOT =====================

    private Map<String, Object> reviewSnapshot(
            SprintReview review
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "goalAchieved",
                review.getGoalAchieved()
        );
        value.put(
                "demoSummary",
                review.getDemoSummary()
        );
        value.put(
                "stakeholderFeedback",
                review.getStakeholderFeedback()
        );
        value.put(
                "acceptedItemSummary",
                review.getAcceptedItemSummary()
        );
        value.put(
                "rejectedItemSummary",
                review.getRejectedItemSummary()
        );
        value.put(
                "note",
                review.getNote()
        );

        return value;
    }

    private Map<String, Object> retrospectiveSnapshot(
            SprintRetrospective retrospective
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "wentWell",
                retrospective.getWentWell()
        );
        value.put(
                "wentWrong",
                retrospective.getWentWrong()
        );
        value.put(
                "improvement",
                retrospective.getImprovement()
        );
        value.put(
                "actionItemsJson",
                retrospective.getActionItemsJson()
        );
        value.put(
                "note",
                retrospective.getNote()
        );

        return value;
    }
}
