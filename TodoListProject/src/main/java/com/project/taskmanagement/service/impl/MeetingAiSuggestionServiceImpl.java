package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.ai.AiProviderClient;
import com.project.taskmanagement.ai.AiRequestType;
import com.project.taskmanagement.ai.ProjectAiContextBuilder;
import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionResponse;
import com.project.taskmanagement.ai.meeting.prompt.MeetingSuggestionPromptBuilder;
import com.project.taskmanagement.entity.AiRequestLog;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AiRequestLogRepository;
import com.project.taskmanagement.service.MeetingAiSuggestionService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingAiSuggestionServiceImpl implements MeetingAiSuggestionService {
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    private final ProjectAiContextBuilder context;
    private final MeetingSuggestionPromptBuilder promptBuilder;
    private final AiProviderClient provider;
    private final ObjectMapper objectMapper;
    private final AiRequestLogRepository logs;

    @Transactional
    public MeetingSuggestionResponse suggest(UUID projectId, MeetingSuggestionRequest request) {
        User user = currentUser.getActiveCurrentUser();
        Project project = access.getProjectOrThrow(projectId);
        access.requireViewAccess(project, user);
        String prompt = promptBuilder.build(request, context.buildProjectContext(projectId));
        String raw = null;
        try {
            raw = provider.generateText(prompt);
            String clean = extractJsonObject(raw);
            MeetingSuggestionResponse response = objectMapper.readValue(clean, MeetingSuggestionResponse.class);
            log(projectId, user.getId(), request.getAdditionalNote(), raw, true, null);
            return response;
        } catch (BusinessException e) {
            log(projectId, user.getId(), request.getAdditionalNote(), raw, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            log(projectId, user.getId(), request.getAdditionalNote(), raw, false, "AI trả về JSON không hợp lệ");
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "AI trả về nội dung meeting không đúng định dạng JSON");
        }
    }

    /** Gemini đôi khi thêm lời dẫn hoặc markdown dù prompt yêu cầu JSON thuần. */
    private String extractJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_EMPTY);
        }
        String text = raw.replace("```json", "").replace("```", "").trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR,
                    "AI trả về nội dung meeting không đúng định dạng JSON");
        }
        return text.substring(start, end + 1);
    }

    private void log(UUID projectId, UUID userId, String prompt, String response, boolean success, String error) {
        AiRequestLog l = new AiRequestLog();
        l.setProjectId(projectId);
        l.setRequestedByUserId(userId);
        l.setRequestType(AiRequestType.MEETING_SUGGESTION);
        l.setPrompt(prompt == null ? "" : prompt);
        l.setResponse(response);
        l.setProvider(provider.providerName());
        l.setModel(provider.modelName());
        l.setSuccess(success);
        l.setErrorMessage(error);
        logs.save(l);
    }
}
