package com.project.taskmanagement.dto.request.sprint;

public record UpdateSprintReviewRequest(

        Boolean goalAchieved,

        String demoSummary,

        String stakeholderFeedback,

        String acceptedItemSummary,

        String rejectedItemSummary,

        String note

) {
}
