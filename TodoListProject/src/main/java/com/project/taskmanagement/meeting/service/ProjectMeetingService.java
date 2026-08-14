package com.project.taskmanagement.meeting.service;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.meeting.dto.CreateProjectMeetingRequest;
import com.project.taskmanagement.meeting.dto.ProjectMeetingResponse;
import com.project.taskmanagement.meeting.dto.UpdateGoogleMeetLinkRequest;
import com.project.taskmanagement.meeting.entity.ProjectMeeting;
import com.project.taskmanagement.meeting.repository.ProjectMeetingRepository;
import com.project.taskmanagement.meeting.validator.GoogleMeetLinkValidator;
import com.project.taskmanagement.service.access.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMeetingService {
    private final ProjectMeetingRepository repo;
    private final GoogleMeetLinkValidator links;
    private final ProjectAccessService access;

    @Transactional
    public ProjectMeetingResponse create(UUID projectId, User user, CreateProjectMeetingRequest r) {
        access.requireSprintManagementAccess(projectId, user);
        if (!r.getEndTime().isAfter(r.getStartTime()))
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Thời gian kết thúc phải sau thời gian bắt đầu");
        ProjectMeeting m = new ProjectMeeting();
        m.setProjectId(projectId);
        m.setTitle(r.getTitle().trim());
        m.setDescription(blank(r.getDescription()));
        m.setMeetingType(r.getMeetingType());
        m.setStartTime(r.getStartTime());
        m.setEndTime(r.getEndTime());
        m.setGoogleMeetLink(links.normalizeAndValidate(r.getGoogleMeetLink()));
        m.setCreatedByUserId(user.getId());
        return to(repo.save(m));
    }

    @Transactional(readOnly = true)
    public List<ProjectMeetingResponse> list(UUID id, User u) {
        access.requireViewAccess(access.getProjectOrThrow(id), u);
        return repo.findAllByProjectIdOrderByStartTimeDesc(id).stream().map(this::to).toList();
    }

    @Transactional(readOnly = true)
    public ProjectMeetingResponse detail(UUID id, UUID mid, User u) {
        access.requireViewAccess(access.getProjectOrThrow(id), u);
        return to(find(id, mid));
    }

    @Transactional
    public ProjectMeetingResponse updateLink(UUID id, UUID mid, UpdateGoogleMeetLinkRequest r, User u) {
        access.requireSprintManagementAccess(id, u);
        ProjectMeeting m = find(id, mid);
        m.setGoogleMeetLink(links.normalizeAndValidate(r.googleMeetLink()));
        return to(m);
    }

    private ProjectMeeting find(UUID id, UUID mid) {
        return repo.findByIdAndProjectId(mid, id).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "Không tìm thấy meeting trong project"));
    }

    private String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private ProjectMeetingResponse to(ProjectMeeting m) {
        return new ProjectMeetingResponse(m.getId(), m.getProjectId(), m.getTitle(), m.getDescription(), m.getMeetingType(), m.getStartTime(), m.getEndTime(), m.getGoogleMeetLink(), m.getGoogleMeetLink() != null && !m.getGoogleMeetLink().isBlank(), m.getCreatedByUserId(), m.getCreatedAt());
    }
}
