package com.project.taskmanagement.dto.response.ai;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProjectAiAskResponse {
    private UUID projectId;
    private String question, answer, provider, model, limitation;
    private Instant createdAt;
    private List<String> keyPoints = List.of(), relatedTasks = List.of(), risks = List.of(), suggestions = List.of();

    public ProjectAiAskResponse() {
    }

    public ProjectAiAskResponse(UUID p, String q, String a, String pr, String m, Instant c) {
        projectId = p;
        question = q;
        answer = a;
        provider = pr;
        model = m;
        createdAt = c;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID v) {
        projectId = v;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String v) {
        question = v;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String v) {
        answer = v;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String v) {
        provider = v;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String v) {
        model = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant v) {
        createdAt = v;
    }

    public String getLimitation() {
        return limitation;
    }

    public void setLimitation(String v) {
        limitation = v;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> v) {
        keyPoints = v;
    }

    public List<String> getRelatedTasks() {
        return relatedTasks;
    }

    public void setRelatedTasks(List<String> v) {
        relatedTasks = v;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> v) {
        risks = v;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> v) {
        suggestions = v;
    }
}
