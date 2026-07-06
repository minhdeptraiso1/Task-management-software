package com.project.taskmanagement.service.activity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.entity.ProjectActivityLog;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectActivityMessageResolver {

    ObjectMapper objectMapper;

    public String resolve(
            ProjectActivityLog activity,
            String performerName
    ) {
        String actor =
                performerName == null
                        || performerName.isBlank()
                        ? "Người dùng"
                        : performerName;

        Map<String, Object> oldValue =
                parseJson(
                        activity.getOldValueJson()
                );

        Map<String, Object> newValue =
                parseJson(
                        activity.getNewValueJson()
                );

        return switch (activity.getAction()) {

            case PROJECT_CREATED -> actor
                    + " đã tạo dự án "
                    + quoted(
                    firstNonNull(
                            newValue,
                            "name",
                            "code"
                    )
            );

            case PROJECT_UPDATED -> actor
                    + " đã cập nhật thông tin dự án";

            case PROJECT_STATUS_CHANGED -> actor
                    + " đã chuyển trạng thái dự án từ "
                    + value(oldValue, "status")
                    + " sang "
                    + value(newValue, "status");

            case PROJECT_DELETED -> actor
                    + " đã xóa dự án "
                    + quoted(
                    value(oldValue, "name")
            );

            case MEMBER_ADDED -> actor
                    + " đã thêm "
                    + quoted(
                    value(newValue, "username")
            )
                    + " vào dự án với vai trò "
                    + value(newValue, "role");

            case MEMBER_REMOVED -> actor
                    + " đã xóa "
                    + quoted(
                    value(oldValue, "username")
            )
                    + " khỏi dự án";

            case MEMBER_ROLE_CHANGED -> actor
                    + " đã đổi vai trò thành viên từ "
                    + value(oldValue, "role")
                    + " sang "
                    + value(newValue, "role");

            case SPRINT_CREATED -> actor
                    + " đã tạo Sprint "
                    + quoted(
                    firstNonNull(
                            newValue,
                            "name",
                            "sprintName"
                    )
            );

            case SPRINT_UPDATED -> actor
                    + " đã cập nhật Sprint";

            case SPRINT_STARTED -> actor
                    + " đã bắt đầu Sprint";

            case SPRINT_COMPLETED -> actor
                    + " đã hoàn thành Sprint";

            case SPRINT_CANCELLED -> actor
                    + " đã hủy Sprint";

            case BACKLOG_ITEM_CREATED -> actor
                    + " đã tạo Backlog Item "
                    + quoted(
                    firstNonNull(
                            newValue,
                            "title",
                            "name"
                    )
            );

            case BACKLOG_ITEM_UPDATED -> actor
                    + " đã cập nhật Backlog Item";

            case BACKLOG_ITEM_DELETED -> actor
                    + " đã xóa Backlog Item "
                    + quoted(
                    firstNonNull(
                            oldValue,
                            "title",
                            "name"
                    )
            );

            case BACKLOG_ITEM_STATUS_CHANGED -> actor
                    + " đã chuyển trạng thái Backlog Item từ "
                    + value(oldValue, "status")
                    + " sang "
                    + value(newValue, "status");

            case BACKLOG_ITEM_PRIORITY_CHANGED -> actor
                    + " đã đổi độ ưu tiên Backlog Item từ "
                    + value(oldValue, "priority")
                    + " sang "
                    + value(newValue, "priority");

            case BACKLOG_ITEM_ADDED_TO_SPRINT -> actor
                    + " đã thêm Backlog Item vào Sprint";

            case BACKLOG_ITEM_REMOVED_FROM_SPRINT -> actor
                    + " đã đưa Backlog Item ra khỏi Sprint";

            case BACKLOG_ITEM_POSITION_CHANGED -> actor
                    + " đã thay đổi vị trí Backlog Item";

            case TASK_CREATED -> actor
                    + " đã tạo Task "
                    + quoted(
                    firstNonNull(
                            newValue,
                            "title",
                            "taskTitle"
                    )
            );

            case TASK_UPDATED -> actor
                    + " đã cập nhật Task";

            case TASK_ASSIGNED -> actor
                    + " đã phân công Task";

            case TASK_UNASSIGNED -> actor
                    + " đã bỏ phân công Task";

            case TASK_STATUS_CHANGED -> actor
                    + " đã chuyển Task từ "
                    + value(oldValue, "status")
                    + " sang "
                    + value(newValue, "status");

            case TASK_PRIORITY_CHANGED -> actor
                    + " đã đổi độ ưu tiên Task từ "
                    + value(oldValue, "priority")
                    + " sang "
                    + value(newValue, "priority");

            case TASK_POSITION_CHANGED -> actor
                    + " đã thay đổi vị trí Task";

            case TASK_DELETED -> actor
                    + " đã xóa Task "
                    + quoted(
                    firstNonNull(
                            oldValue,
                            "title",
                            "taskTitle"
                    )
            );

            case TASK_IMPORTED -> actor
                    + " đã import Task từ Excel";

            case TASK_MOVED_TO_SPRINT -> actor
                    + " đã đưa Task vào Sprint";

            case TASK_REMOVED_FROM_SPRINT -> actor
                    + " đã đưa Task ra khỏi Sprint";

            case BUG_CREATED -> actor
                    + " đã tạo Bug";

            case COMMENT_CREATED -> actor
                    + " đã thêm bình luận";

            case COMMENT_UPDATED -> actor
                    + " đã cập nhật bình luận";

            case COMMENT_DELETED -> actor
                    + " đã xóa bình luận";

            case TIME_LOG_CREATED -> actor
                    + " đã ghi nhận thời gian làm việc";

            case TIME_LOG_UPDATED -> actor
                    + " đã cập nhật thời gian làm việc";

            case TIME_LOG_DELETED -> actor
                    + " đã xóa bản ghi thời gian";
        };
    }

    // ===================== JSON =====================

    private Map<String, Object> parseJson(
            String json
    ) {
        if (json == null
                || json.isBlank()) {

            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<
                            LinkedHashMap<String, Object>
                            >() {
                    }
            );

        } catch (Exception exception) {
            return new LinkedHashMap<>();
        }
    }

    // ===================== VALUE =====================

    private String value(
            Map<String, Object> values,
            String key
    ) {
        if (values == null
                || key == null) {

            return "không xác định";
        }

        Object value =
                values.get(key);

        return value == null
                ? "không xác định"
                : String.valueOf(value);
    }

    private String firstNonNull(
            Map<String, Object> values,
            String... keys
    ) {
        if (values == null
                || keys == null) {

            return null;
        }

        for (String key : keys) {
            Object value =
                    values.get(key);

            if (value != null
                    && !String.valueOf(value).isBlank()) {

                return String.valueOf(value);
            }
        }

        return null;
    }

    private String quoted(
            String value
    ) {
        if (value == null
                || value.isBlank()
                || value.equals("không xác định")) {

            return "";
        }

        return "\""
                + value
                + "\"";
    }
}