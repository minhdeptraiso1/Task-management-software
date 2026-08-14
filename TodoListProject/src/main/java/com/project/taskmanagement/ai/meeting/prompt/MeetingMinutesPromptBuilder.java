package com.project.taskmanagement.ai.meeting.prompt;
import com.project.taskmanagement.ai.meeting.dto.*; import org.springframework.stereotype.Component;
@Component public class MeetingMinutesPromptBuilder {
 public String build(MeetingMinutesRequest r,String context){return """
Bạn là trợ lý tạo biên bản họp Agile/Scrum. Chuyển ghi chú thô thành biên bản tiếng Việt có cấu trúc.
Chỉ dùng thông tin trong ghi chú và context; không bịa quyết định, deadline hay người phụ trách. Action item chỉ là bản nháp, không phải task đã tạo. Không nói đã ghi database, tạo task, Google Meet hoặc gửi email.
Meeting type: %s | title: %s | time: %s | participants: %s
Ghi chú thô:
%s
Project context:
%s
Trả về JSON thuần, không markdown: {"title":"string","meetingType":"string","meetingTime":"string","participants":["string"],"overview":"string","discussedItems":["string"],"decisions":["string"],"unresolvedIssues":["string"],"actionItemDrafts":[{"content":"string","suggestedAssignee":"string","suggestedDueDate":"string","priority":"LOW|MEDIUM|HIGH","sourceNote":"string"}],"relatedRisks":["string"],"finalSummary":"string"}
""".formatted(r.getMeetingType(),blank(r.getMeetingTitle()),blank(r.getMeetingTime()),blank(r.getParticipants()),r.getRawNotes(),context);}
 private String blank(String v){return v==null||v.isBlank()?"Chưa có thông tin":v.trim();}
}
