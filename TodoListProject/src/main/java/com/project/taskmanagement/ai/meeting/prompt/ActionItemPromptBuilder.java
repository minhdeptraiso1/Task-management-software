package com.project.taskmanagement.ai.meeting.prompt;

import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionRequest;
import org.springframework.stereotype.Component;

@Component
public class ActionItemPromptBuilder {
    public String build(ActionItemSuggestionRequest r, String context) {
        return """
                Bạn là trợ lý Agile. Hãy phân tích nội dung cuộc họp và đề xuất tối đa 10 action item quan trọng nhất.
                Chỉ dùng nội dung họp và context. Không bịa task, người phụ trách hoặc deadline. Nếu không chắc, dùng 'Chưa xác định' và null. Priority chỉ LOW, MEDIUM, HIGH; confidence từ 0 đến 1; temporaryId dạng AI-1. Gộp item trùng nhau. Đây chỉ là đề xuất, không phải task đã tạo và không được ghi database.
                Meeting type: %s | title: %s | ghi chú: %s
                Nội dung họp: %s
                Project context: %s
                Trả về JSON thuần: {"meetingTitle":"string","summary":"string","actionItems":[{"temporaryId":"AI-1","title":"string","description":"string","suggestedAssigneeName":"string","suggestedAssigneeId":null,"suggestedDueDate":"string","priority":"LOW|MEDIUM|HIGH","relatedTaskId":null,"relatedTaskTitle":"string","reason":"string","confidence":0.8}],"warnings":["string"]}
                """.formatted(r.getMeetingType(), blank(r.getMeetingTitle()), blank(r.getAdditionalNote()), r.getMeetingContent(), context);
    }

    private String blank(String v) {
        return v == null || v.isBlank() ? "Chưa có thông tin" : v.trim();
    }
}
 