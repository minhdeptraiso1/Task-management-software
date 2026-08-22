package com.project.taskmanagement.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.gemini")
public class GeminiAiProperties {
    private String apiKey = "";
    private String model = "gemini-2.5-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private int timeoutSeconds = 30;
    private int maxOutputTokens = 8192;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String v) {
        apiKey = v;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String v) {
        model = v;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String v) {
        baseUrl = v;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int v) {
        timeoutSeconds = v;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int v) {
        maxOutputTokens = v;
    }
}
 