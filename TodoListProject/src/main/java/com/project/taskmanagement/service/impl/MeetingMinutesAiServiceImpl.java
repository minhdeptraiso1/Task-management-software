package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.ai.AiProviderClient;
import com.project.taskmanagement.ai.AiRequestType;
import com.project.taskmanagement.ai.ProjectAiContextBuilder;
import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesResponse;
import com.project.taskmanagement.ai.meeting.prompt.MeetingMinutesPromptBuilder;
import com.project.taskmanagement.entity.AiRequestLog;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AiRequestLogRepository;
import com.project.taskmanagement.service.MeetingMinutesAiService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingMinutesAiServiceImpl implements MeetingMinutesAiService {
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    private final ProjectAiContextBuilder context;
    private final MeetingMinutesPromptBuilder prompts;
    private final AiProviderClient provider;
    private final ObjectMapper mapper;
    private final AiRequestLogRepository logs;

    @Transactional
    public MeetingMinutesResponse create(UUID projectId, MeetingMinutesRequest request) {
        User user = currentUser.getActiveCurrentUser();
        Project p = access.getProjectOrThrow(projectId);
        access.requireViewAccess(p, user);
        String prompt = prompts.build(request, context.buildProjectContext(projectId));
        try {
            String raw = provider.generateText(prompt);
            String json = raw.replace("```json", "").replace("```", "").trim();
            MeetingMinutesResponse out = mapper.readValue(json, MeetingMinutesResponse.class);
            save(projectId, user.getId(), request.getRawNotes(), raw, true, null);
            return out;
        } catch (BusinessException e) {
            save(projectId, user.getId(), request.getRawNotes(), null, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            save(projectId, user.getId(), request.getRawNotes(), null, false, "AI trả về JSON không hợp lệ");
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "AI trả về biên bản họp không đúng định dạng JSON");
        }
    }

    private void save(UUID projectId, UUID userId, String prompt, String response, boolean ok, String error) {
        AiRequestLog l = new AiRequestLog();
        l.setProjectId(projectId);
        l.setRequestedByUserId(userId);
        l.setRequestType(AiRequestType.MEETING_MINUTES);
        l.setPrompt(prompt);
        l.setResponse(response);
        l.setProvider(provider.providerName());
        l.setModel(provider.modelName());
        l.setSuccess(ok);
        l.setErrorMessage(error);
        logs.save(l);
    }
}
