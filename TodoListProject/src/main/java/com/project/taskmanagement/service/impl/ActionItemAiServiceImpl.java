package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.ai.AiProviderClient;
import com.project.taskmanagement.ai.AiRequestType;
import com.project.taskmanagement.ai.ProjectAiContextBuilder;
import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionResponse;
import com.project.taskmanagement.ai.meeting.prompt.ActionItemPromptBuilder;
import com.project.taskmanagement.entity.AiRequestLog;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AiRequestLogRepository;
import com.project.taskmanagement.service.ActionItemAiService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActionItemAiServiceImpl implements ActionItemAiService {
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    private final ProjectAiContextBuilder context;
    private final ActionItemPromptBuilder prompts;
    private final AiProviderClient provider;
    private final ObjectMapper mapper;
    private final AiRequestLogRepository logs;

    @Transactional
    public ActionItemSuggestionResponse suggest(UUID projectId, ActionItemSuggestionRequest request) {
        User u = currentUser.getActiveCurrentUser();
        Project p = access.getProjectOrThrow(projectId);
        access.requireViewAccess(p, u);
        String prompt = prompts.build(request, context.buildProjectContext(projectId));
        try {
            String raw = provider.generateText(prompt);
            String json = raw.replace("```json", "").replace("```", "").trim();
            ActionItemSuggestionResponse out = mapper.readValue(json, ActionItemSuggestionResponse.class);
            save(projectId, u.getId(), request.getMeetingContent(), raw, true, null);
            return out;
        } catch (BusinessException e) {
            save(projectId, u.getId(), request.getMeetingContent(), null, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            save(projectId, u.getId(), request.getMeetingContent(), null, false, "AI trả về JSON không hợp lệ");
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "AI trả về action item không đúng định dạng JSON");
        }
    }

    private void save(UUID p, UUID u, String prompt, String response, boolean ok, String err) {
        AiRequestLog l = new AiRequestLog();
        l.setProjectId(p);
        l.setRequestedByUserId(u);
        l.setRequestType(AiRequestType.ACTION_ITEMS);
        l.setPrompt(prompt);
        l.setResponse(response);
        l.setProvider(provider.providerName());
        l.setModel(provider.modelName());
        l.setSuccess(ok);
        l.setErrorMessage(err);
        logs.save(l);
    }
}
