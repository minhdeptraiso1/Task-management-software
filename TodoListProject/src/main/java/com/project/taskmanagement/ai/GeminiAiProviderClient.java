package com.project.taskmanagement.ai;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GeminiAiProviderClient implements AiProviderClient {
    private final GeminiAiProperties properties;
    private final WebClient.Builder webClientBuilder;

    public String providerName() {
        return "GEMINI";
    }

    public String modelName() {
        return properties.getModel();
    }

    public String generateText(String prompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank())
            throw new BusinessException(ErrorCode.AI_API_KEY_MISSING);
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", properties.getMaxOutputTokens(), "responseMimeType", "application/json")
            );
            Map<?, ?> response = webClientBuilder.baseUrl(properties.getBaseUrl()).build().post().uri("/v1beta/models/{model}:generateContent", properties.getModel()).header("x-goog-api-key", properties.getApiKey()).contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().bodyToMono(Map.class).timeout(Duration.ofSeconds(properties.getTimeoutSeconds())).block();
            String text = extractText(response);
            if (text == null || text.isBlank()) throw new BusinessException(ErrorCode.AI_RESPONSE_EMPTY);
            return text.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    private String extractText(Map<?, ?> root) {
        if (root == null || !(root.get("candidates") instanceof List<?> cs) || cs.isEmpty() || !(cs.get(0) instanceof Map<?, ?> c) || !(c.get("content") instanceof Map<?, ?> co) || !(co.get("parts") instanceof List<?> ps))
            return null;
        return ps.stream().filter(Map.class::isInstance).map(Map.class::cast).map(p -> p.get("text")).filter(Objects::nonNull).map(Object::toString).findFirst().orElse(null);
    }
} 
