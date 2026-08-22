package com.project.taskmanagement.ai;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.access.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultProjectAiContextBuilder implements ProjectAiContextBuilder {
    private final ProjectAccessService access;
    private final SprintRepository sprints;
    private final BacklogItemRepository backlogs;
    private final TaskRepository tasks;
    private final ProjectMemberRepository members;

    public String buildProjectContext(UUID id) {
        Project p = access.getProjectOrThrow(id);
        Sprint s = sprints.findByProjectIdAndStatus(id, SprintStatus.ACTIVE).orElse(null);
        StringBuilder b = new StringBuilder();
        b.append("Dự án: ").append(p.getCode()).append(" - ").append(p.getName()).append("; trạng thái ").append(p.getStatus()).append("; thời gian ").append(p.getStartDate()).append(" đến ").append(p.getEndDate()).append(".\n");
        b.append("Thành viên: ").append(members.countByProjectId(id)).append("; backlog item: ").append(backlogs.countByProjectId(id)).append(".\n");
        if (s != null)
            b.append("Sprint đang chạy: ").append(s.getName()).append("; mục tiêu: ").append(Optional.ofNullable(s.getGoal()).orElse("chưa đặt mục tiêu")).append("; ").append(s.getStartDate()).append(" đến ").append(s.getEndDate()).append(".\n");
        else b.append("Hiện không có sprint đang chạy.\n");
        b.append("Task theo trạng thái: TODO=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.TODO)).append(", IN_PROGRESS=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.IN_PROGRESS)).append(", IN_REVIEW=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.IN_REVIEW)).append(", DONE=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.DONE)).append(", BLOCKED=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.BLOCKED)).append(", CANCELLED=").append(tasks.countByProjectIdAndStatus(id, TaskStatus.CANCELLED)).append("; quá hạn=").append(tasks.countOverdueByProjectId(id, LocalDate.now(), List.of(TaskStatus.DONE, TaskStatus.CANCELLED))).append(".");
        return b.toString();
    }
}
