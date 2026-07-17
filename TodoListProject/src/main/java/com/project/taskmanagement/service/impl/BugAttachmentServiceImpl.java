package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.bug.BugAttachmentResponse;
import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.entity.BugAttachment;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugAttachmentRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.BugAttachmentService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.BugValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    static String UPLOAD_DIR = "uploads/bugs";

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public BugAttachmentResponse upload(UUID projectId, UUID bugId, MultipartFile file) throws IOException {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectMemberAndBug(projectId, bugId, currentUser);
        BugValidator.validateEditable(bug);

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_ATTACHMENT_INVALID);
        }

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Lưu file vật lý
        String fileId = UUID.randomUUID().toString();
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = fileId + (fileExtension.isEmpty() ? "" : "." + fileExtension);
        Path targetLocation = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        BugAttachment attachment = BugAttachment.builder()
                .projectId(projectId)
                .bugId(bugId)
                .uploadedByUserId(currentUser.getId())
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .sizeBytes(file.getSize())
                .storagePath(targetLocation.toString())
                .build();
        attachment.setId(UUID.fromString(fileId));

        BugAttachment saved = bugAttachmentRepository.save(attachment);

        log(projectId, saved.getId(), ProjectActivityAction.BUG_ATTACHMENT_UPLOADED,
                currentUser.getId(), null, snapshot(saved));

        notifyBug(projectId, bug, currentUser, NotificationType.BUG_ATTACHMENT_ADDED,
                "Bug có đính kèm mới", currentUser.getUsername() + " đã đính kèm tệp \"" + originalFileName + "\" vào Bug: " + bug.getTitle());

        return toResponse(saved, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public List<BugAttachmentResponse> getAll(UUID projectId, UUID bugId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        return bugAttachmentRepository.findAllByProjectIdAndBugIdOrderByCreatedAtDesc(projectId, bugId)
                .stream()
                .map(att -> toResponse(att, currentUser))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BugAttachmentResponse getById(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugAttachment attachment = bugAttachmentRepository.findByIdAndProjectIdAndBugId(attachmentId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND));
        return toResponse(attachment, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.core.io.Resource loadFileAsResource(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);

        BugAttachment attachment = bugAttachmentRepository.findByIdAndProjectIdAndBugId(attachmentId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND));

        try {
            Path filePath = Paths.get(attachment.getStoragePath()).toAbsolutePath().normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_ATTACHMENT_LIST, key = "{#projectId,#bugId}")
    public void delete(UUID projectId, UUID bugId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectMemberAndBug(projectId, bugId, currentUser);
        BugValidator.validateEditable(bug);

        BugAttachment attachment = bugAttachmentRepository.findByIdAndProjectIdAndBugId(attachmentId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_ATTACHMENT_NOT_FOUND));

        if (!attachment.getUploadedByUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.BUG_ATTACHMENT_ACCESS_DENIED);
        }

        Map<String, Object> oldValue = snapshot(attachment);

        // Xóa file vật lý
        try {
            Path filePath = Paths.get(attachment.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log nhưng không chặn luồng xóa DB
        }

        attachment.markDeleted(currentUser.getUsername());
        bugAttachmentRepository.save(attachment);

        log(projectId, attachment.getId(), ProjectActivityAction.BUG_ATTACHMENT_DELETED,
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

    private BugAttachmentResponse toResponse(BugAttachment att, User currentUser) {
        User creator = userRepository.findById(att.getUploadedByUserId()).orElse(null);
        boolean owner = att.getUploadedByUserId().equals(currentUser.getId());
        String downloadUrl = "/projects/" + att.getProjectId() + "/bugs/" + att.getBugId() + "/attachments/" + att.getId() + "/download";
        return new BugAttachmentResponse(
                att.getId(),
                att.getBugId(),
                att.getUploadedByUserId(),
                creator == null ? null : creator.getUsername(),
                att.getOriginalFileName(),
                att.getContentType(),
                att.getSizeBytes(),
                downloadUrl,
                owner,
                att.getCreatedAt()
        );
    }

    private Map<String, Object> snapshot(BugAttachment att) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", att.getId());
        value.put("bugId", att.getBugId());
        value.put("uploadedByUserId", att.getUploadedByUserId());
        value.put("originalFileName", att.getOriginalFileName());
        value.put("contentType", att.getContentType());
        value.put("sizeBytes", att.getSizeBytes());
        return value;
    }

    private void log(UUID projectId, UUID entityId, ProjectActivityAction action, UUID actor,
                     Map<String, Object> oldValue, Map<String, Object> newValue) {
        projectActivityService.log(new ProjectActivityCommand(projectId, ActivityEntityType.BUG_ATTACHMENT,
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

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
