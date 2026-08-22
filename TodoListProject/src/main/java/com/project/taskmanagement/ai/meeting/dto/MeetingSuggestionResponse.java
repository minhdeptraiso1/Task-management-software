package com.project.taskmanagement.ai.meeting.dto;

import java.util.List;
import java.util.UUID;

public class MeetingSuggestionResponse {
    private String title, purpose, summary;
    private List<String> agenda = List.of(), discussionQuestions = List.of(), relatedRisks = List.of();
    private List<RelatedTaskItem> relatedTasks = List.of();

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String v) {
        purpose = v;
    }

    public List<String> getAgenda() {
        return agenda;
    }

    public void setAgenda(List<String> v) {
        agenda = v;
    }

    public List<String> getDiscussionQuestions() {
        return discussionQuestions;
    }

    public void setDiscussionQuestions(List<String> v) {
        discussionQuestions = v;
    }

    public List<String> getRelatedRisks() {
        return relatedRisks;
    }

    public void setRelatedRisks(List<String> v) {
        relatedRisks = v;
    }

    public List<RelatedTaskItem> getRelatedTasks() {
        return relatedTasks;
    }

    public void setRelatedTasks(List<RelatedTaskItem> v) {
        relatedTasks = v;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String v) {
        summary = v;
    }

    public static class RelatedTaskItem {
        private UUID taskId;
        private String title, status, assigneeName, reason;

        public UUID getTaskId() {
            return taskId;
        }

        public void setTaskId(UUID v) {
            taskId = v;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String v) {
            title = v;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String v) {
            status = v;
        }

        public String getAssigneeName() {
            return assigneeName;
        }

        public void setAssigneeName(String v) {
            assigneeName = v;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String v) {
            reason = v;
        }
    }
}
 