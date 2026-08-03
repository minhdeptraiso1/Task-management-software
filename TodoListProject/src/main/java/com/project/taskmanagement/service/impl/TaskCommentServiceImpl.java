package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.taskcomment.CreateTaskCommentRequest;
import com.project.taskmanagement.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.project.taskmanagement.dto.realtime.KanbanRealtimeEventType;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentPageResponse;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentReplyResponse;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskComment;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TaskCommentRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskCommentService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.cache.CacheEvictService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.realtime.KanbanRealtimePublisher;
import com.project.taskmanagement.service.mention.CommentMentionResolver;
import com.project.taskmanagement.service.mention.CommentMentionParser;
import com.project.taskmanagement.service.mention.MentionedUser;
import com.project.taskmanagement.service.validation.TaskCommentValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskCommentServiceImpl
        implements TaskCommentService {

    TaskRepository taskRepository;
    TaskCommentRepository taskCommentRepository;
    UserLookupHelper userLookupHelper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityService projectActivityService;
    NotificationService notificationService;
    CommentMentionResolver commentMentionResolver;
    KanbanRealtimePublisher kanbanRealtimePublisher;
    CacheEvictService cacheEvictService;

    // ===================== CREATE =====================

    @Override
    @Transactional
    public TaskCommentResponse create(
            UUID projectId,
            UUID taskId,
            CreateTaskCommentRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        projectAccessService
                .requireTaskCommentAccess(
                        project,
                        task,
                        currentUser
                );

        String content =
                TaskCommentValidator
                        .normalizeContent(
                                request.content()
                        );

        TaskComment parentComment = null;

        if (request.parentCommentId()
                != null) {

            parentComment =
                    taskCommentRepository
                            .findById(
                                    request.parentCommentId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode
                                                    .TASK_COMMENT_PARENT_NOT_FOUND
                                    )
                            );

            if (!parentComment.getTaskId()
                    .equals(taskId)) {

                throw new BusinessException(
                        ErrorCode
                                .TASK_COMMENT_PARENT_MISMATCH
                );
            }

            TaskCommentValidator
                    .validateParentDepth(
                            parentComment
                    );
        }

        TaskComment comment =
                TaskComment.builder()
                        .taskId(taskId)
                        .userId(
                                currentUser.getId()
                        )
                        .parentCommentId(
                                parentComment != null
                                        ? parentComment.getId()
                                        : null
                        )
                        .content(content)
                        .editedAt(null)
                        .build();

        TaskComment savedComment =
                taskCommentRepository.save(
                        comment
                );

        List<MentionedUser> mentionedUsers =
                commentMentionResolver
                        .resolveProjectMentions(
                                projectId,
                                savedComment.getContent()
                        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "taskId",
                taskId
        );

        newValue.put(
                "parentCommentId",
                savedComment
                        .getParentCommentId()
        );

        newValue.put(
                "content",
                savedComment.getContent()
        );

        newValue.put(
                "mentionedUsernames",
                mentionedUsers.stream()
                        .map(MentionedUser::username)
                        .toList()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.COMMENT,
                        savedComment.getId(),
                        ProjectActivityAction
                                .COMMENT_CREATED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        sendCommentNotification(
                projectId,
                task,
                savedComment,
                parentComment,
                currentUser,
                mentionedUsers
        );

        sendMentionNotifications(
                projectId,
                task,
                savedComment,
                currentUser,
                mentionedUsers
        );

        kanbanRealtimePublisher.publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_COMMENTED,
                currentUser.getId(),
                currentUser.getUsername()
        );

        cacheEvictService.evictTaskComments(projectId, taskId);

        return toCommentResponse(
                projectId,
                savedComment,
                currentUser
        );
    }

    // ===================== GET COMMENTS =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.TASK_COMMENT_LIST,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #taskId" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public TaskCommentPageResponse getComments(
            UUID projectId,
            UUID taskId,
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

        getTaskOrThrow(
                projectId,
                taskId
        );

        Page<TaskComment> page =
                taskCommentRepository
                        .findAllByTaskIdAndParentCommentIdIsNullOrderByCreatedAtDesc(
                                taskId,
                                pageable
                        );

        List<TaskComment> rootComments = page.getContent();
        List<UUID> rootIds = rootComments.stream().map(TaskComment::getId).toList();
        List<TaskComment> replies = rootIds.isEmpty()
                ? List.of()
                : taskCommentRepository.findAllByTaskIdAndParentCommentIdInOrderByCreatedAtAsc(
                        taskId,
                        rootIds
                );
        Map<UUID, List<TaskComment>> repliesByParentId = replies.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TaskComment::getParentCommentId,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        List<UUID> authorIds = new ArrayList<>();
        rootComments.forEach(comment -> authorIds.add(comment.getUserId()));
        replies.forEach(reply -> authorIds.add(reply.getUserId()));
        Map<UUID, User> usersById = userLookupHelper.findUserMap(authorIds);
        List<TaskComment> allComments = new ArrayList<>(rootComments);
        allComments.addAll(replies);
        Map<String, String> mentionedUsernames = resolveMentionedUsernameMap(projectId, allComments);
        boolean canModerate = projectAccessService.canModerateTaskComments(projectId, currentUser);

        List<TaskCommentResponse> responses = rootComments.stream()
                .map(comment -> toCommentResponse(
                        projectId,
                        comment,
                        currentUser,
                        canModerate,
                        usersById,
                        repliesByParentId.getOrDefault(comment.getId(), List.of()),
                        mentionedUsernames
                ))
                .toList();

        return new TaskCommentPageResponse(
                responses,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }

    // ===================== UPDATE =====================

    @Override
    @Transactional
    public TaskCommentResponse update(
            UUID projectId,
            UUID taskId,
            UUID commentId,
            UpdateTaskCommentRequest request
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

        getTaskOrThrow(
                projectId,
                taskId
        );

        TaskComment comment =
                getCommentOrThrow(
                        taskId,
                        commentId
                );

        if (!comment.getUserId()
                .equals(currentUser.getId())) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_COMMENT_ACCESS_DENIED
            );
        }

        String oldContent =
                comment.getContent();

        String newContent =
                TaskCommentValidator
                        .normalizeContent(
                                request.content()
                        );

        if (oldContent.equals(newContent)) {
            return toCommentResponse(
                    projectId,
                    comment,
                    currentUser
            );
        }

        comment.setContent(
                newContent
        );

        comment.setEditedAt(
                Instant.now()
        );

        TaskComment savedComment =
                taskCommentRepository.save(
                        comment
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.COMMENT,
                        savedComment.getId(),
                        ProjectActivityAction
                                .COMMENT_UPDATED,
                        currentUser.getId(),
                        Map.of(
                                "content",
                                oldContent
                        ),
                        Map.of(
                                "content",
                                newContent
                        )
                )
        );

        /*
         * Không gửi notification khi chỉ sửa nội dung,
         * tránh spam thành viên.
         */

        cacheEvictService.evictTaskComments(projectId, taskId);

        return toCommentResponse(
                projectId,
                savedComment,
                currentUser
        );
    }

    // ===================== DELETE =====================

    @Override
    @Transactional
    public void delete(
            UUID projectId,
            UUID taskId,
            UUID commentId
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

        getTaskOrThrow(
                projectId,
                taskId
        );

        TaskComment comment =
                getCommentOrThrow(
                        taskId,
                        commentId
                );

        boolean isOwner =
                comment.getUserId()
                        .equals(
                                currentUser.getId()
                        );

        boolean canModerate =
                projectAccessService
                        .canModerateTaskComments(
                                projectId,
                                currentUser
                        );

        if (!isOwner && !canModerate) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_COMMENT_ACCESS_DENIED
            );
        }

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "taskId",
                taskId
        );

        oldValue.put(
                "userId",
                comment.getUserId()
        );

        oldValue.put(
                "parentCommentId",
                comment.getParentCommentId()
        );

        oldValue.put(
                "content",
                comment.getContent()
        );

        comment.markDeleted(
                currentUser.getUsername()
        );

        taskCommentRepository.save(
                comment
        );

        cacheEvictService.evictTaskComments(projectId, taskId);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.COMMENT,
                        commentId,
                        ProjectActivityAction
                                .COMMENT_DELETED,
                        currentUser.getId(),
                        oldValue,
                        null
                )
        );
    }

    // ===================== REPLIES =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.TASK_COMMENT_LIST,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #taskId" +
                    " + ':' + #commentId + ':replies'"
    )
    public List<TaskCommentReplyResponse> getReplies(
            UUID projectId,
            UUID taskId,
            UUID commentId
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

        getTaskOrThrow(
                projectId,
                taskId
        );

        TaskComment parentComment =
                getCommentOrThrow(
                        taskId,
                        commentId
                );

        TaskCommentValidator
                .validateRootComment(parentComment);

        List<TaskComment> replies = taskCommentRepository
                .findAllByParentCommentIdOrderByCreatedAtAsc(commentId);
        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                replies.stream().map(TaskComment::getUserId).toList()
        );
        Map<String, String> mentionedUsernames = resolveMentionedUsernameMap(projectId, replies);
        boolean canModerate = projectAccessService.canModerateTaskComments(projectId, currentUser);

        return replies.stream()
                .map(reply -> toReplyResponse(
                        projectId,
                        reply,
                        currentUser,
                        canModerate,
                        usersById,
                        mentionedUsernames
                ))
                .toList();
    }

    @Override
    @Transactional
    public TaskCommentReplyResponse createReply(
            UUID projectId,
            UUID taskId,
            UUID commentId,
            CreateTaskCommentRequest request
    ) {
        TaskCommentResponse response =
                create(
                        projectId,
                        taskId,
                        new CreateTaskCommentRequest(
                                commentId,
                                request.content()
                        )
                );

        return new TaskCommentReplyResponse(
                response.id(),
                response.taskId(),
                response.userId(),
                response.username(),
                response.email(),
                response.parentCommentId(),
                response.content(),
                response.edited(),
                response.editedAt(),
                response.createdAt(),
                response.updatedAt(),
                response.canEdit(),
                response.canDelete(),
                response.mentionedUsernames()
        );
    }

    // ===================== RESPONSE =====================

    private TaskCommentResponse toCommentResponse(
            UUID projectId,
            TaskComment comment,
            User currentUser
    ) {
        List<TaskComment> replies = taskCommentRepository
                .findAllByParentCommentIdOrderByCreatedAtAsc(comment.getId());
        List<UUID> authorIds = new ArrayList<>();
        authorIds.add(comment.getUserId());
        replies.forEach(reply -> authorIds.add(reply.getUserId()));
        return toCommentResponse(
                projectId,
                comment,
                currentUser,
                projectAccessService.canModerateTaskComments(projectId, currentUser),
                userLookupHelper.findUserMap(authorIds),
                replies,
                resolveMentionedUsernameMap(
                        projectId,
                        java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(comment),
                                replies.stream()
                        ).toList()
                )
        );
    }

    private TaskCommentResponse toCommentResponse(
            UUID projectId,
            TaskComment comment,
            User currentUser,
            boolean canModerate,
            Map<UUID, User> usersById,
            List<TaskComment> replyComments,
            Map<String, String> mentionedUsernames
    ) {
        User author = userLookupHelper.getOrNull(usersById, comment.getUserId());

        boolean canEdit =
                comment.getUserId()
                        .equals(
                                currentUser.getId()
                        );

        boolean canDelete = canEdit || canModerate;

        List<TaskCommentReplyResponse> replies = replyComments.stream()
                .map(reply -> toReplyResponse(
                        projectId,
                        reply,
                        currentUser,
                        canModerate,
                        usersById,
                        mentionedUsernames
                ))
                .toList();

        return new TaskCommentResponse(
                comment.getId(),
                comment.getTaskId(),
                comment.getUserId(),
                author != null
                        ? author.getUsername()
                        : null,
                author != null
                        ? author.getEmail()
                        : null,
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getEditedAt() != null,
                comment.getEditedAt(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                canEdit,
                canDelete,
                replies.size(),
                getMentionedUsernames(comment.getContent(), mentionedUsernames),
                replies
        );
    }

    private TaskCommentReplyResponse toReplyResponse(
            UUID projectId,
            TaskComment reply,
            User currentUser
    ) {
        return toReplyResponse(
                projectId,
                reply,
                currentUser,
                projectAccessService.canModerateTaskComments(projectId, currentUser),
                userLookupHelper.findUserMap(Collections.singletonList(reply.getUserId())),
                resolveMentionedUsernameMap(projectId, List.of(reply))
        );
    }

    private TaskCommentReplyResponse toReplyResponse(
            UUID projectId,
            TaskComment reply,
            User currentUser,
            boolean canModerate,
            Map<UUID, User> usersById,
            Map<String, String> mentionedUsernames
    ) {
        User author = userLookupHelper.getOrNull(usersById, reply.getUserId());

        boolean canEdit =
                reply.getUserId()
                        .equals(
                                currentUser.getId()
                        );

        boolean canDelete = canEdit || canModerate;

        return new TaskCommentReplyResponse(
                reply.getId(),
                reply.getTaskId(),
                reply.getUserId(),
                author != null
                        ? author.getUsername()
                        : null,
                author != null
                        ? author.getEmail()
                        : null,
                reply.getParentCommentId(),
                reply.getContent(),
                reply.getEditedAt() != null,
                reply.getEditedAt(),
                reply.getCreatedAt(),
                reply.getUpdatedAt(),
                canEdit,
                canDelete,
                getMentionedUsernames(reply.getContent(), mentionedUsernames)
        );
    }

    // ===================== NOTIFICATION =====================

    private void sendCommentNotification(
            UUID projectId,
            Task task,
            TaskComment newComment,
            TaskComment parentComment,
            User actor,
            List<MentionedUser> mentionedUsers
    ) {
        Set<UUID> recipients =
                new LinkedHashSet<>();

        /*
         * Người được giao Task nhận notification.
         */
        if (task.getAssigneeUserId()
                != null) {

            recipients.add(
                    task.getAssigneeUserId()
            );
        }

        /*
         * Nếu là reply, người viết comment cha
         * cũng nhận notification.
         */
        if (parentComment != null) {
            recipients.add(
                    parentComment.getUserId()
            );
        }

        /*
         * Reporter có thể cần theo dõi Task.
         */
        if (task.getReporterUserId()
                != null) {

            recipients.add(
                    task.getReporterUserId()
            );
        }

        recipients.remove(
                actor.getId()
        );

        mentionedUsers.stream()
                .map(MentionedUser::userId)
                .forEach(recipients::remove);

        if (recipients.isEmpty()) {
            return;
        }

        String title =
                parentComment == null
                        ? "Task có bình luận mới"
                        : "Có phản hồi bình luận Task";

        String content =
                actor.getUsername()
                        + (
                        parentComment == null
                                ? " đã bình luận trong Task "
                                : " đã phản hồi trong Task "
                )
                        + task.getTitle();

        notificationService.create(
                new NotificationCommand(
                        NotificationType.TASK_COMMENTED,
                        title,
                        content,
                        actor.getId(),
                        projectId,
                        ActivityEntityType.COMMENT,
                        newComment.getId(),
                        new ArrayList<>(
                                recipients
                        )
                )
        );
    }

    private void sendMentionNotifications(
            UUID projectId,
            Task task,
            TaskComment newComment,
            User actor,
            List<MentionedUser> mentionedUsers
    ) {
        List<UUID> recipients =
                mentionedUsers.stream()
                        .map(MentionedUser::userId)
                        .filter(userId ->
                                !userId.equals(actor.getId())
                        )
                        .distinct()
                        .toList();

        if (recipients.isEmpty()) {
            return;
        }

        notificationService.create(
                new NotificationCommand(
                        NotificationType.TASK_MENTIONED,
                        "Bạn được nhắc trong Task",
                        actor.getUsername()
                                + " đã nhắc bạn trong Task "
                                + task.getTitle(),
                        actor.getId(),
                        projectId,
                        ActivityEntityType.COMMENT,
                        newComment.getId(),
                        recipients
                )
        );
    }

    private Map<String, String> resolveMentionedUsernameMap(
            UUID projectId,
            Collection<TaskComment> comments
    ) {
        if (comments == null || comments.isEmpty()) {
            return Map.of();
        }

        String combinedContent = comments.stream()
                .map(TaskComment::getContent)
                .filter(Objects::nonNull)
                .filter(content -> !content.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));

        if (CommentMentionParser.parseUsernames(combinedContent).isEmpty()) {
            return Map.of();
        }

        return commentMentionResolver.resolveProjectMentions(projectId, combinedContent)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        user -> user.username().toLowerCase(Locale.ROOT),
                        MentionedUser::username,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private List<String> getMentionedUsernames(
            String content,
            Map<String, String> mentionedUsernames
    ) {
        if (mentionedUsernames == null || mentionedUsernames.isEmpty()) {
            return List.of();
        }

        return CommentMentionParser.parseUsernames(content).stream()
                .map(username -> mentionedUsernames.get(username.toLowerCase(Locale.ROOT)))
                .filter(Objects::nonNull)
                .toList();
    }

    // ===================== HELPER =====================

    private Task getTaskOrThrow(
            UUID projectId,
            UUID taskId
    ) {
        return taskRepository
                .findByIdAndProjectId(
                        taskId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TASK_NOT_FOUND
                        )
                );
    }

    private TaskComment getCommentOrThrow(
            UUID taskId,
            UUID commentId
    ) {
        return taskCommentRepository
                .findByIdAndTaskId(
                        commentId,
                        taskId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .TASK_COMMENT_NOT_FOUND
                        )
                );
    }
}
