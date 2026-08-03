package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.bug.BugAttachmentResponse;
import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.entity.BugAttachment;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugAttachmentRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.BugAttachmentService;
import com.project.taskmanagement.service.FileStorageService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.StoredFile;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.BugValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugAttachmentServiceImpl implements BugAttachmentService {

    BugAttachmentRepository bugAttachmentRepository;
    BugRepository bugRepository;
    UserRepository userRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    NotificationService notificationService;
    FileStorageService fileStorageService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public BugAttachmentResponse upload(UUID projectId, UUID bugId, MultipartFile file) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectMemberAndBug(projectId, bugId, currentUser);
        BugValidator.validateEditable(bug);

        StoredFile storedFile =
                fileStorageService.store(
                        projectId,
                        AttachmentEntityType.BUG,
                        bugId,
                        file
                );

        try {
            BugAttachment attachment = BugAttachment.builder()
                    .projectId(projectId)
                    .bugId(bugId)
                    .uploadedByUserId(currentUser.getId())
                    .originalFileName(storedFile.originalFileName())
                    .storedFileName(storedFile.storedFileName())
                    .contentType(resolveContentType(storedFile.contentType()))
                    .sizeBytes(storedFile.sizeBytes())
                    .storagePath(storedFile.storagePath())
                    .build();

            BugAttachment saved = bugAttachmentRepository.save(attachment);

            log(projectId, saved.getId(), ProjectActivityAction.BUG_ATTACHMENT_UPLOADED,
                    currentUser.getId(), null, snapshot(saved));
            notifyBugAttachmentUploaded(projectId, bug, saved, currentUser);

            return toResponse(saved, currentUser, bug);
        } catch (RuntimeException ex) {
            fileStorageService.deletePhysicalFileIfExists(storedFile.storagePath());
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public List<BugAttachmentResponse> getAll(UUID projectId, UUID bugId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectViewAndBug(projectId, bugId, currentUser);

        return bugAttachmentRepository.findAllByProjectIdAndBugIdOrderByCreatedAtDesc(projectId, bugId)
                .stream()
                .map(attachment -> toResponse(attachment, currentUser, bug))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BugAttachmentResponse getById(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectViewAndBug(projectId, bugId, currentUser);
        BugAttachment attachment = getAttachmentOrThrow(projectId, bugId, attachmentId);

        return toResponse(attachment, currentUser, bug);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadFileAsResource(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugAttachment attachment = getAttachmentOrThrow(projectId, bugId, attachmentId);

        return fileStorageService
                .load(
                        attachment.getStoragePath(),
                        attachment.getOriginalFileName(),
                        attachment.getContentType(),
                        attachment.getSizeBytes()
                )
                .resource();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public void delete(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        ProjectMember membership = projectAccessService.getMembershipOrThrow(projectId, currentUser.getId());
        Bug bug = getBugOrThrow(projectId, bugId);
        BugValidator.validateEditable(bug);

        BugAttachment attachment = getAttachmentOrThrow(projectId, bugId, attachmentId);
        if (!canDeleteAttachment(attachment, currentUser, membership)) {
            throw new BusinessException(ErrorCode.BUG_ATTACHMENT_ACCESS_DENIED);
        }

        Map<String, Object> oldValue = snapshot(attachment);
        fileStorageService.deletePhysicalFileIfExists(attachment.getStoragePath());

        attachment.markDeleted(currentUser.getUsername());
        bugAttachmentRepository.save(attachment);

        log(projectId, attachment.getId(), ProjectActivityAction.BUG_ATTACHMENT_DELETED,
                currentUser.getId(), oldValue, null);
    }

    private Bug requireProjectMemberAndBug(UUID projectId, UUID bugId, User user) {
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.BUG_ATTACHMENT_ACCESS_DENIED);
        }

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

    private BugAttachment getAttachmentOrThrow(UUID projectId, UUID bugId, UUID attachmentId) {
        return bugAttachmentRepository.findByIdAndProjectIdAndBugId(attachmentId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND));
    }

    private BugAttachmentResponse toResponse(BugAttachment attachment, User currentUser, Bug bug) {
        User creator = userRepository.findById(attachment.getUploadedByUserId()).orElse(null);
        ProjectMember membership = projectAccessService
                .findMembership(attachment.getProjectId(), currentUser.getId())
                .orElse(null);
        String downloadUrl = "/projects/" + attachment.getProjectId()
                + "/bugs/" + attachment.getBugId()
                + "/attachments/" + attachment.getId()
                + "/download";

        return new BugAttachmentResponse(
                attachment.getId(),
                attachment.getBugId(),
                attachment.getUploadedByUserId(),
                creator == null ? null : creator.getUsername(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                downloadUrl,
                isBugEditable(bug) && canDeleteAttachment(attachment, currentUser, membership),
                attachment.getCreatedAt()
        );
    }

    private Map<String, Object> snapshot(BugAttachment attachment) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", attachment.getId());
        value.put("bugId", attachment.getBugId());
        value.put("uploadedByUserId", attachment.getUploadedByUserId());
        value.put("originalFileName", attachment.getOriginalFileName());
        value.put("storedFileName", attachment.getStoredFileName());
        value.put("contentType", attachment.getContentType());
        value.put("sizeBytes", attachment.getSizeBytes());
        value.put("storagePath", attachment.getStoragePath());
        return value;
    }

    private void log(UUID projectId, UUID entityId, ProjectActivityAction action, UUID actorUserId,
                     Map<String, Object> oldValue, Map<String, Object> newValue) {
        projectActivityService.log(new ProjectActivityCommand(
                projectId,
                ActivityEntityType.BUG_ATTACHMENT,
                entityId,
                action,
                actorUserId,
                oldValue,
                newValue
        ));
    }

    private void notifyBugAttachmentUploaded(UUID projectId, Bug bug, BugAttachment attachment, User actor) {
        Set<UUID> recipients = new LinkedHashSet<>();
        recipients.add(bug.getReporterUserId());
        recipients.add(bug.getAssigneeUserId());
        recipients.remove(null);
        recipients.remove(actor.getId());

        if (recipients.isEmpty()) {
            return;
        }

        notificationService.create(new NotificationCommand(
                NotificationType.BUG_ATTACHMENT_ADDED,
                "Bug có tệp đính kèm mới",
                actor.getUsername() + " đã đính kèm tệp \"" + attachment.getOriginalFileName()
                        + "\" vào Bug: " + bug.getTitle(),
                actor.getId(),
                projectId,
                ActivityEntityType.BUG_ATTACHMENT,
                attachment.getId(),
                recipients
        ));
    }

    private String resolveContentType(String contentType) {
        return StringUtils.hasText(contentType)
                ? contentType
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private boolean canDeleteAttachment(BugAttachment attachment, User currentUser, ProjectMember membership) {
        if (attachment.getUploadedByUserId().equals(currentUser.getId())) {
            return true;
        }

        return membership != null && isProjectModerator(membership.getRole());
    }

    private boolean isProjectModerator(ProjectMemberRole role) {
        return role == ProjectMemberRole.OWNER
                || role == ProjectMemberRole.PROJECT_MANAGER
                || role == ProjectMemberRole.SCRUM_MASTER;
    }

    private boolean isBugEditable(Bug bug) {
        return bug.getStatus() != BugStatus.CLOSED
                && bug.getStatus() != BugStatus.CANCELLED;
    }

}
