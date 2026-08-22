package com.project.taskmanagement.meeting.repository;

import com.project.taskmanagement.meeting.entity.ProjectMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMeetingRepository extends JpaRepository<ProjectMeeting, UUID> {
    List<ProjectMeeting> findAllByProjectIdOrderByStartTimeDesc(UUID projectId);

    Optional<ProjectMeeting> findByIdAndProjectId(UUID id, UUID projectId);
}
