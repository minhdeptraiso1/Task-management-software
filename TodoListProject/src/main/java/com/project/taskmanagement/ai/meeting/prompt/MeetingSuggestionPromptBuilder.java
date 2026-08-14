package com.project.taskmanagement.ai.meeting.prompt;
import com.project.taskmanagement.ai.meeting.dto.*; import org.springframework.stereotype.Component;
@Component public class MeetingSuggestionPromptBuilder {
 public String build(MeetingSuggestionRequest request,String context){ return """
Bạn là trợ lý Meeting cho hệ thống Agile/Scrum. Hãy gợi ý nội dung cuộc họp bằng tiếng Việt dựa duy nhất trên context được cung cấp.
Không bịa task, người dùng, deadline; không tạo/sửa dữ liệu, không nói đã tạo meeting hay gửi email. Nếu thiếu dữ liệu hãy nói rõ.
Loại meeting: %s
Ghi chú bổ sung: %s
Project context:
%s
Trả về đúng JSON object, không markdown theo schema: {"title":"string","purpose":"string","agenda":["string"],"discussionQuestions":["string"],"relatedRisks":["string"],"relatedTasks":[{"taskId":"UUID hoặc null","title":"string","status":"string","assigneeName":"string","reason":"string"}],"summary":"string"}
""".formatted(request.getMeetingType(),request.getAdditionalNote()==null?"Không có":request.getAdditionalNote().trim(),context); }
}
