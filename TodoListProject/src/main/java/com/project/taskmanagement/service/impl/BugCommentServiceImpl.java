package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.bug.CreateBugCommentRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugCommentRequest;
import com.project.taskmanagement.dto.response.bug.BugCommentResponse;
import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.entity.BugComment;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugCommentRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.service.BugCommentService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.util.TextNormalizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugCommentServiceImpl implements BugCommentService {

    BugCommentRepository bugCommentRepository;
    BugRepository bugRepository;
    UserLookupHelper userLookupHelper;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_COMMENT_LIST, key = "{#projectId,#bugId}")
    public BugCommentResponse create(UUID projectId, UUID bugId, CreateBugCommentRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectMemberAndBug(projectId, bugId, currentUser);
        String content = normalizeRequired(request.content(), ErrorCode.VALIDATION_ERROR);

        if (request.parentId() != null) {
            BugComment parent = getCommentOrThrow(projectId, bugId, request.parentId());
            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER);
            }
        }

        BugComment comment = BugComment.builder()
                .projectId(projectId)
                .bugId(bugId)
                .parentId(request.parentId())
                .authorUserId(currentUser.getId())
                .content(content)
                .build();

        BugComment saved = bugCommentRepository.save(comment);
        log(projectId, saved.getId(), ProjectActivityAction.BUG_COMMENT_CREATED,
                currentUser.getId(), null, snapshot(saved));
        notifyBug(projectId, bug, currentUser, NotificationType.BUG_COMMENTED,
                "Bug có bình luận mới", currentUser.getUsername() + " đã bình luận Bug: " + bug.getTitle());
        return toResponse(saved, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_COMMENT_LIST, key = "{#projectId,#bugId}")
    public List<BugCommentResponse> getAll(UUID projectId, UUID bugId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        List<BugComment> comments = bugCommentRepository
                .findAllByProjectIdAndBugIdOrderByCreatedAtAsc(projectId, bugId);
        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                comments.stream().map(BugComment::getAuthorUserId).toList()
        );
        return comments
                .stream()
                .map(comment -> toResponse(comment, currentUser, usersById))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_COMMENT_LIST, key = "{#projectId,#bugId}")
    public BugCommentResponse update(UUID projectId, UUID bugId, UUID commentId, UpdateBugCommentRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugComment comment = getCommentOrThrow(projectId, bugId, commentId);
        validateOwner(comment.getAuthorUserId(), currentUser, ErrorCode.BUG_COMMENT_ACCESS_DENIED);
        Map<String, Object> oldValue = snapshot(comment);
        comment.setContent(normalizeRequired(request.content(), ErrorCode.VALIDATION_ERROR));
        BugComment saved = bugCommentRepository.save(comment);
        log(projectId, saved.getId(), ProjectActivityAction.BUG_COMMENT_UPDATED,
                currentUser.getId(), oldValue, snapshot(saved));
        return toResponse(saved, currentUser);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_COMMENT_LIST, key = "{#projectId,#bugId}")
    public void delete(UUID projectId, UUID bugId, UUID commentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugComment comment = getCommentOrThrow(projectId, bugId, commentId);
        validateOwner(comment.getAuthorUserId(), currentUser, ErrorCode.BUG_COMMENT_ACCESS_DENIED);
        Map<String, Object> oldValue = snapshot(comment);
        comment.markDeleted(currentUser.getUsername());
        bugCommentRepository.save(comment);
        log(projectId, comment.getId(), ProjectActivityAction.BUG_COMMENT_DELETED,
                currentUser.getId(), oldValue, null);
    }

    private Bug requireProjectMemberAndBug(UUID projectId, UUID bugId, User user) {
        projectAccessService.getMembershipOrThrow(projectId, user.getId());
        return getBugOrThrow(projectId, bugId);
    }

    private Bug requireProjectViewAndBug(UUID projectId, UUID bugId, User user) {
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, user);
        return getBugOrThrow(projectId, bugId);
    }

    private Bug getBugOrThrow(UUID projectId, UUID bugId) {
        return bugRepository.findByIdAndProjectId(bugId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_NOT_FOUND));
    }

    private BugComment getCommentOrThrow(UUID projectId, UUID bugId, UUID commentId) {
        return bugCommentRepository.findByIdAndProjectIdAndBugId(commentId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_COMMENT_NOT_FOUND));
    }

    private void validateOwner(UUID ownerId, User user, ErrorCode errorCode) {
        if (!ownerId.equals(user.getId())) {
            throw new BusinessException(errorCode);
        }
    }

    private BugCommentResponse toResponse(BugComment comment, User currentUser) {
        return toResponse(
                comment,
                currentUser,
                userLookupHelper.findUserMap(List.of(comment.getAuthorUserId()))
        );
    }

    private BugCommentResponse toResponse(
            BugComment comment,
            User currentUser,
            Map<UUID, User> usersById
    ) {
        User author = userLookupHelper.getOrNull(usersById, comment.getAuthorUserId());
        boolean owner = comment.getAuthorUserId().equals(currentUser.getId());
        return new BugCommentResponse(comment.getId(), comment.getBugId(), comment.getParentId(),
                comment.getAuthorUserId(), author == null ? null : author.getUsername(),
                author == null ? null : author.getEmail(), comment.getContent(), owner, owner,
                comment.getCreatedAt(), comment.getUpdatedAt());
    }

    private Map<String, Object> snapshot(BugComment comment) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", comment.getId());
        value.put("bugId", comment.getBugId());
        value.put("parentId", comment.getParentId());
        value.put("authorUserId", comment.getAuthorUserId());
        value.put("content", comment.getContent());
        return value;
    }

    private void log(UUID projectId, UUID entityId, ProjectActivityAction action, UUID actor,
                     Map<String, Object> oldValue, Map<String, Object> newValue) {
        projectActivityService.log(new ProjectActivityCommand(projectId, ActivityEntityType.BUG_COMMENT,
                entityId, action, actor, oldValue, newValue));
    }

    private void notifyBug(UUID projectId, Bug bug, User actor, NotificationType type, String title, String content) {
        Set<UUID> recipients = new LinkedHashSet<>();
        recipients.add(bug.getReporterUserId());
        recipients.add(bug.getAssigneeUserId());
        recipients.remove(null);
        recipients.remove(actor.getId());
        if (!recipients.isEmpty()) {
            notificationService.create(new NotificationCommand(type, title, content, actor.getId(), projectId,
                    ActivityEntityType.BUG, bug.getId(), recipients));
        }
    }

    private String normalizeRequired(String value, ErrorCode errorCode) {
        String normalized = TextNormalizer.trim(value);
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException(errorCode);
        }
        return normalized;
    }
}
