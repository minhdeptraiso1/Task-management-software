package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.bug.CreateBugEvidenceRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugEvidenceRequest;
import com.project.taskmanagement.dto.response.bug.BugEvidenceResponse;
import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.entity.BugEvidence;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugEvidenceRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.BugEvidenceService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
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
public class BugEvidenceServiceImpl implements BugEvidenceService {

    BugEvidenceRepository bugEvidenceRepository;
    BugRepository bugRepository;
    UserRepository userRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_EVIDENCE_LIST, key = "{#projectId,#bugId}")
    public BugEvidenceResponse create(UUID projectId, UUID bugId, CreateBugEvidenceRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = requireProjectMemberAndBug(projectId, bugId, currentUser);

        BugEvidence evidence = BugEvidence.builder()
                .projectId(projectId)
                .bugId(bugId)
                .createdByUserId(currentUser.getId())
                .title(normalizeRequired(request.title(), ErrorCode.VALIDATION_ERROR))
                .stepsToReproduce(TextNormalizer.trimToNull(request.stepsToReproduce()))
                .expectedResult(TextNormalizer.trimToNull(request.expectedResult()))
                .actualResult(TextNormalizer.trimToNull(request.actualResult()))
                .environment(TextNormalizer.trimToNull(request.environment()))
                .note(TextNormalizer.trimToNull(request.note()))
                .build();

        BugEvidence saved = bugEvidenceRepository.save(evidence);
        log(projectId, saved.getId(), ProjectActivityAction.BUG_EVIDENCE_CREATED,
                currentUser.getId(), null, snapshot(saved));
        notifyBug(projectId, bug, currentUser, NotificationType.BUG_EVIDENCE_ADDED,
                "Bug có bằng chứng mới", currentUser.getUsername() + " đã thêm bằng chứng cho Bug: " + bug.getTitle());
        return toResponse(saved, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_EVIDENCE_LIST, key = "{#projectId,#bugId}")
    public List<BugEvidenceResponse> getAll(UUID projectId, UUID bugId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        return bugEvidenceRepository.findAllByProjectIdAndBugIdOrderByCreatedAtDesc(projectId, bugId)
                .stream()
                .map(evidence -> toResponse(evidence, currentUser))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_EVIDENCE_LIST, key = "{#projectId,#bugId}")
    public BugEvidenceResponse update(UUID projectId, UUID bugId, UUID evidenceId, UpdateBugEvidenceRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugEvidence evidence = getEvidenceOrThrow(projectId, bugId, evidenceId);
        validateOwner(evidence.getCreatedByUserId(), currentUser, ErrorCode.BUG_EVIDENCE_ACCESS_DENIED);

        Map<String, Object> oldValue = snapshot(evidence);
        if (request.title() != null) {
            evidence.setTitle(normalizeRequired(request.title(), ErrorCode.VALIDATION_ERROR));
        }
        evidence.setStepsToReproduce(TextNormalizer.trimToNull(request.stepsToReproduce()));
        evidence.setExpectedResult(TextNormalizer.trimToNull(request.expectedResult()));
        evidence.setActualResult(TextNormalizer.trimToNull(request.actualResult()));
        evidence.setEnvironment(TextNormalizer.trimToNull(request.environment()));
        evidence.setNote(TextNormalizer.trimToNull(request.note()));

        BugEvidence saved = bugEvidenceRepository.save(evidence);
        log(projectId, saved.getId(), ProjectActivityAction.BUG_EVIDENCE_UPDATED,
                currentUser.getId(), oldValue, snapshot(saved));
        return toResponse(saved, currentUser);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.BUG_EVIDENCE_LIST, key = "{#projectId,#bugId}")
    public void delete(UUID projectId, UUID bugId, UUID evidenceId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireProjectViewAndBug(projectId, bugId, currentUser);
        BugEvidence evidence = getEvidenceOrThrow(projectId, bugId, evidenceId);
        validateOwner(evidence.getCreatedByUserId(), currentUser, ErrorCode.BUG_EVIDENCE_ACCESS_DENIED);

        Map<String, Object> oldValue = snapshot(evidence);
        evidence.markDeleted(currentUser.getUsername());
        bugEvidenceRepository.save(evidence);
        log(projectId, evidence.getId(), ProjectActivityAction.BUG_EVIDENCE_DELETED,
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

    private BugEvidence getEvidenceOrThrow(UUID projectId, UUID bugId, UUID evidenceId) {
        return bugEvidenceRepository.findByIdAndProjectIdAndBugId(evidenceId, projectId, bugId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUG_EVIDENCE_NOT_FOUND));
    }

    private void validateOwner(UUID ownerId, User user, ErrorCode errorCode) {
        if (!ownerId.equals(user.getId())) {
            throw new BusinessException(errorCode);
        }
    }

    private BugEvidenceResponse toResponse(BugEvidence evidence, User currentUser) {
        User creator = userRepository.findById(evidence.getCreatedByUserId()).orElse(null);
        boolean owner = evidence.getCreatedByUserId().equals(currentUser.getId());
        return new BugEvidenceResponse(evidence.getId(), evidence.getBugId(), evidence.getCreatedByUserId(),
                creator == null ? null : creator.getUsername(), evidence.getTitle(),
                evidence.getStepsToReproduce(), evidence.getExpectedResult(), evidence.getActualResult(),
                evidence.getEnvironment(), evidence.getNote(), owner, owner,
                evidence.getCreatedAt(), evidence.getUpdatedAt());
    }

    private Map<String, Object> snapshot(BugEvidence evidence) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", evidence.getId());
        value.put("bugId", evidence.getBugId());
        value.put("createdByUserId", evidence.getCreatedByUserId());
        value.put("title", evidence.getTitle());
        value.put("stepsToReproduce", evidence.getStepsToReproduce());
        value.put("expectedResult", evidence.getExpectedResult());
        value.put("actualResult", evidence.getActualResult());
        value.put("environment", evidence.getEnvironment());
        value.put("note", evidence.getNote());
        return value;
    }

    private void log(UUID projectId, UUID entityId, ProjectActivityAction action, UUID actor,
                     Map<String, Object> oldValue, Map<String, Object> newValue) {
        projectActivityService.log(new ProjectActivityCommand(projectId, ActivityEntityType.BUG_EVIDENCE,
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
