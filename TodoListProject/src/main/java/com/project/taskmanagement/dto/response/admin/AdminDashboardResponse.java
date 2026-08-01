package com.project.taskmanagement.dto.response.admin;

public record AdminDashboardResponse(

        AdminUserSummaryResponse userSummary,

        AdminProjectSummaryResponse projectSummary,

        AdminSprintSummaryResponse sprintSummary,

        AdminTaskSummaryResponse taskSummary,

        AdminBugSummaryResponse bugSummary,

        AdminAttachmentSummaryResponse attachmentSummary,

        AdminSystemSummaryResponse systemSummary
) {
}
