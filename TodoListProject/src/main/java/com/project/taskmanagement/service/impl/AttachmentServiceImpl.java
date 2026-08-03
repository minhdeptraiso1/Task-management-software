package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.config.FileSecurityProperties;
import com.project.taskmanagement.dto.response.attachment.AttachmentPageResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentResponse;
import com.project.taskmanagement.dto.response.attachment.AttachmentUsageResponse;
import com.project.taskmanagement.dto.response.attachment.FileSecuritySummaryResponse;
import com.project.taskmanagement.entity.Attachment;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AttachmentRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.AttachmentService;
import com.project.taskmanagement.service.FileStorageService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.attachment.AttachmentEntityResolver;
import com.project.taskmanagement.service.attachment.AttachmentPermissionService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.LoadedFile;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.model.StoredFile;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import com.project.taskmanagement.service.validation.FileSecurityValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentServiceImpl implements AttachmentService {

    AttachmentRepository attachmentRepository;
    UserRepository userRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    FileStorageService fileStorageService;
    ProjectActivityService projectActivityService;
    AttachmentEntityResolver attachmentEntityResolver;
    AttachmentPermissionService attachmentPermissionService;
    FileSecurityProperties fileSecurityProperties;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ATTACHMENT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.ATTACHMENT_USAGE, allEntries = true),
            @CacheEvict(value = CacheNames.GLOBAL_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true)
    })
    public AttachmentResponse upload(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId,
            MultipartFile file
    ) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireUpload(project, currentUser);

        attachmentEntityResolver.validateEntityExists(projectId, entityType, entityId);
        validateAttachmentLimits(projectId, entityType, entityId, file.getSize());

        StoredFile storedFile = fileStorageService.store(projectId, entityType, entityId, file);
        try {
            Attachment attachment = Attachment.builder()
                    .projectId(projectId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .uploadedByUserId(currentUser.getId())
                    .originalFileName(storedFile.originalFileName())
                    .storedFileName(storedFile.storedFileName())
                    .contentType(storedFile.contentType())
                    .extension(storedFile.extension())
                    .sizeBytes(storedFile.sizeBytes())
                    .storagePath(storedFile.storagePath())
                    .build();

            Attachment saved = attachmentRepository.save(attachment);
            projectActivityService.log(new ProjectActivityCommand(
                    projectId,
                    ActivityEntityType.ATTACHMENT,
                    saved.getId(),
                    ProjectActivityAction.ATTACHMENT_UPLOADED,
                    currentUser.getId(),
                    null,
                    snapshot(saved)
            ));

            systemAuditService.log(new SystemAuditCommand(
                    currentUser.getId(),
                    SystemAuditAction.FILE_UPLOADED,
                    SystemAuditResourceType.ATTACHMENT,
                    saved.getId(),
                    auditRequestHelper.getClientIp(httpServletRequest),
                    auditRequestHelper.getUserAgent(httpServletRequest),
                    null,
                    snapshot(saved),
                    true,
                    null
            ));

            return toResponse(saved, currentUser);
        } catch (RuntimeException exception) {
            fileStorageService.deletePhysicalFileIfExists(storedFile.storagePath());
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ATTACHMENT_LIST,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|entityType=' + #entityType" +
                    " + '|entityId=' + #entityId" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public AttachmentPageResponse getAttachments(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId,
            Pageable pageable
    ) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        attachmentEntityResolver.validateEntityExists(projectId, entityType, entityId);

        Page<AttachmentResponse> page = attachmentRepository
                .findAllByProjectIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        projectId,
                        entityType,
                        entityId,
                        pageable
                )
                .map(attachment -> toResponse(attachment, currentUser));

        return AttachmentPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ATTACHMENT_LIST,
            key = "'project:' + #projectId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':user:' + @currentUserService.getActiveCurrentUser().getUsername()"
    )
    public AttachmentPageResponse getProjectAttachments(UUID projectId, Pageable pageable) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        Page<AttachmentResponse> page = attachmentRepository
                .findAllByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(attachment -> toResponse(attachment, currentUser));

        return AttachmentPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public LoadedFile download(UUID projectId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        Attachment attachment = getAttachmentOrThrow(projectId, attachmentId);
        attachmentEntityResolver.validateEntityExists(projectId, attachment.getEntityType(), attachment.getEntityId());

        systemAuditService.log(new SystemAuditCommand(
                currentUser.getId(),
                SystemAuditAction.FILE_DOWNLOADED,
                SystemAuditResourceType.ATTACHMENT,
                attachment.getId(),
                auditRequestHelper.getClientIp(httpServletRequest),
                auditRequestHelper.getUserAgent(httpServletRequest),
                null,
                snapshot(attachment),
                true,
                null
        ));

        return fileStorageService.load(
                attachment.getStoragePath(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes()
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ATTACHMENT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.ATTACHMENT_USAGE, allEntries = true),
            @CacheEvict(value = CacheNames.GLOBAL_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true)
    })
    public void delete(UUID projectId, UUID attachmentId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        Attachment attachment = getAttachmentOrThrow(projectId, attachmentId);
        attachmentPermissionService.requireDelete(attachment, currentUser);

        Map<String, Object> oldValue = snapshot(attachment);
        attachment.markDeleted(currentUser.getUsername());
        attachmentRepository.save(attachment);

        projectActivityService.log(new ProjectActivityCommand(
                projectId,
                ActivityEntityType.ATTACHMENT,
                attachment.getId(),
                ProjectActivityAction.ATTACHMENT_DELETED,
                currentUser.getId(),
                oldValue,
                null
        ));

        systemAuditService.log(new SystemAuditCommand(
                currentUser.getId(),
                SystemAuditAction.FILE_DELETED,
                SystemAuditResourceType.ATTACHMENT,
                attachment.getId(),
                auditRequestHelper.getClientIp(httpServletRequest),
                auditRequestHelper.getUserAgent(httpServletRequest),
                oldValue,
                null,
                true,
                null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ATTACHMENT_USAGE,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId"
    )
    public AttachmentUsageResponse getProjectUsage(UUID projectId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        long usedBytes = safeLong(attachmentRepository.sumSizeBytesByProjectId(projectId));
        long maxBytes = fileSecurityProperties.maxProjectStorageBytesOrDefault();
        long remainingBytes = Math.max(0, maxBytes - usedBytes);
        double usageRate = maxBytes == 0 ? 0 : usedBytes * 100.0 / maxBytes;

        return new AttachmentUsageResponse(projectId, usedBytes, maxBytes, remainingBytes, usageRate);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ATTACHMENT_SECURITY_SUMMARY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId"
    )
    public FileSecuritySummaryResponse getFileSecuritySummary(UUID projectId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        attachmentPermissionService.requireView(project, currentUser);

        ArrayList<String> allowedExtensions = new ArrayList<>(FileSecurityValidator.ALLOWED_MIME_BY_EXTENSION.keySet());
        ArrayList<String> blockedExtensions = new ArrayList<>(FileSecurityValidator.BLOCKED_EXTENSIONS);
        Collections.sort(allowedExtensions);
        Collections.sort(blockedExtensions);

        return new FileSecuritySummaryResponse(
                fileSecurityProperties.maxFileSizeBytesOrDefault(),
                fileSecurityProperties.maxFilesPerEntityOrDefault(),
                fileSecurityProperties.maxProjectStorageBytesOrDefault(),
                fileSecurityProperties.keepDeletedFileDaysOrDefault(),
                allowedExtensions,
                blockedExtensions
        );
    }

    private Attachment getAttachmentOrThrow(UUID projectId, UUID attachmentId) {
        return attachmentRepository.findByIdAndProjectId(attachmentId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private void validateAttachmentLimits(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId,
            long newFileSize
    ) {
        long currentEntityFiles = attachmentRepository.countByProjectIdAndEntityTypeAndEntityId(
                projectId,
                entityType,
                entityId
        );

        if (currentEntityFiles >= fileSecurityProperties.maxFilesPerEntityOrDefault()) {
            throw new BusinessException(ErrorCode.ATTACHMENT_ENTITY_LIMIT_EXCEEDED);
        }

        long currentProjectStorage = safeLong(attachmentRepository.sumSizeBytesByProjectId(projectId));
        if (currentProjectStorage + newFileSize > fileSecurityProperties.maxProjectStorageBytesOrDefault()) {
            throw new BusinessException(ErrorCode.PROJECT_STORAGE_LIMIT_EXCEEDED);
        }
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private AttachmentResponse toResponse(Attachment attachment, User currentUser) {
        User uploader = userRepository.findById(attachment.getUploadedByUserId()).orElse(null);
        boolean canDelete = attachmentPermissionService.canDelete(attachment, currentUser);

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getProjectId(),
                attachment.getEntityType(),
                attachment.getEntityId(),
                attachment.getUploadedByUserId(),
                uploader == null ? null : uploader.getUsername(),
                uploader == null ? null : uploader.getEmail(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getExtension(),
                attachment.getSizeBytes(),
                buildDownloadUrl(attachment),
                canDelete,
                attachment.getCreatedAt()
        );
    }

    private String buildDownloadUrl(Attachment attachment) {
        return "/projects/" + attachment.getProjectId()
                + "/attachments/" + attachment.getId()
                + "/download";
    }

    private Map<String, Object> snapshot(Attachment attachment) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", attachment.getId());
        value.put("entityType", attachment.getEntityType());
        value.put("entityId", attachment.getEntityId());
        value.put("originalFileName", attachment.getOriginalFileName());
        value.put("storedFileName", attachment.getStoredFileName());
        value.put("contentType", attachment.getContentType());
        value.put("extension", attachment.getExtension());
        value.put("sizeBytes", attachment.getSizeBytes());
        value.put("uploadedByUserId", attachment.getUploadedByUserId());
        return value;
    }
}
