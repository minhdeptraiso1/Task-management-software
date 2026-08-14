package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.ai.AiProviderClient;
import com.project.taskmanagement.ai.AiRequestType;
import com.project.taskmanagement.ai.ProjectAiContextBuilder;
import com.project.taskmanagement.dto.request.ai.ProjectAiAskRequest;
import com.project.taskmanagement.dto.response.ai.ProjectAiAskResponse;
import com.project.taskmanagement.entity.AiRequestLog;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.AiRequestLogRepository;
import com.project.taskmanagement.service.ProjectAiAssistantService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAiAssistantServiceImpl implements ProjectAiAssistantService {
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    private final ProjectAiContextBuilder context;
    private final AiProviderClient provider;
    private final AiRequestLogRepository logs;
    private final ObjectMapper mapper;

    @Transactional
    public ProjectAiAskResponse ask(UUID id, ProjectAiAskRequest r) {
        User u = currentUser.getActiveCurrentUser();
        Project p = access.getProjectOrThrow(id);
        access.requireViewAccess(p, u);
        String q = r.question().trim();
        String prompt = "Bạn là trợ lý dữ liệu Project Agile. Chỉ dùng CONTEXT project hiện tại, không bịa và không tạo/sửa/xóa dữ liệu. Nếu thiếu thông tin, ghi rõ limitation. Trả JSON thuần theo schema answer,keyPoints,relatedTasks,risks,suggestions,limitation. Ghi chú: " + (r.additionalContext() == null ? "Không có" : r.additionalContext().trim()) + "\nCONTEXT:\n" + context.buildProjectContext(id) + "\nCÂU HỎI:\n" + q;
        try {
            String raw = provider.generateText(prompt);
            ProjectAiAskResponse out = mapper.readValue(raw.replace("```json", "").replace("```", "").trim(), ProjectAiAskResponse.class);
            out.setProjectId(id);
            out.setQuestion(q);
            out.setProvider(provider.providerName());
            out.setModel(provider.modelName());
            out.setCreatedAt(Instant.now());
            save(id, u.getId(), q, raw, true, null);
            return out;
        } catch (BusinessException e) {
            save(id, u.getId(), q, null, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            save(id, u.getId(), q, null, false, e.getMessage());
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "AI trả về câu trả lời không đúng định dạng JSON");
        }
    }

    private void save(UUID p, UUID u, String q, String a, boolean ok, String e) {
        AiRequestLog l = new AiRequestLog();
        l.setProjectId(p);
        l.setRequestedByUserId(u);
        l.setRequestType(AiRequestType.PROJECT_QA);
        l.setPrompt(q);
        l.setResponse(a);
        l.setProvider(provider.providerName());
        l.setModel(provider.modelName());
        l.setSuccess(ok);
        l.setErrorMessage(e);
        logs.save(l);
    }
}
