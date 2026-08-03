package com.project.taskmanagement.config;

public final class CacheNames {

    private CacheNames() {
    }

    // Cache names follow one stable snake_case convention because they are also
    // used as keys in app.cache.ttl. Do not create literal cache names in services.

    // ===================== USER =====================

    public static final String USER_DETAIL = "user_detail";
    public static final String USER_CURRENT = "user_current";
    public static final String USER_SEARCH = "user_search";
    public static final String USER_PROJECT_CANDIDATE_SEARCH = "user_project_candidate_search";

    // ===================== PROJECT =====================

    public static final String PROJECT_DETAIL = "project_detail";
    public static final String PROJECT_SEARCH = "project_search";
    public static final String PROJECT_SEARCH_RESULT = "project_search_result";
    public static final String PROJECT_MEMBERS = "project_members";
    public static final String PROJECT_MEMBER_LIST = PROJECT_MEMBERS;

    // ===================== SPRINT =====================

    public static final String CURRENT_SPRINT = "current_sprint";
    public static final String SPRINT_DETAIL = "sprint_detail";
    public static final String SPRINT_SEARCH = "sprint_search";
    public static final String SPRINT_LIST = SPRINT_SEARCH;
    public static final String BACKLOG_ITEM_DETAIL = "backlog_item_detail";
    public static final String BACKLOG_ITEM_SEARCH = "backlog_item_search";
    public static final String BACKLOG_LIST = BACKLOG_ITEM_SEARCH;

    // ===================== TASK =====================

    public static final String TASK_DETAIL = "task_detail";

    public static final String TASK_SEARCH = "task_search";

    public static final String SPRINT_KANBAN = "sprint_kanban";

    public static final String SPRINT_TASK_STATISTICS = "sprint_task_statistics";

    public static final String SPRINT_BURNDOWN = "sprint_burndown";

    public static final String SPRINT_CAPACITY = "sprint_capacity";

    public static final String SPRINT_HEALTH = "sprint_health";

    public static final String SPRINT_RISKS = "sprint_risks";

    public static final String SPRINT_PROGRESS = "sprint_progress";

    public static final String SPRINT_CLOSING_REPORT = "sprint_closing_report";

    public static final String SPRINT_REVIEW = "sprint_review";

    public static final String SPRINT_RETROSPECTIVE = "sprint_retrospective";

    public static final String TASK_TIME_SUMMARY = "task_time_summary";

    public static final String TASK_TIME_LOG_LIST = "task_time_log_list";

    public static final String TASK_COMMENT_LIST = "task_comment_list";

    // ===================== DASHBOARD =====================

    public static final String MY_DASHBOARD = "my_dashboard";
    public static final String MY_TASK_SEARCH = "my_task_search";
    public static final String MY_TIME_SUMMARY = "my_time_summary";

    public static final String PROJECT_DASHBOARD = "project_dashboard";
    public static final String PROJECT_DASHBOARD_WORKLOAD = "project_dashboard_workload";
    public static final String PROJECT_DASHBOARD_RECENT_ACTIVITY = "project_dashboard_recent_activity";

    // ===================== ACTIVITY =====================

    public static final String PROJECT_ACTIVITY_SEARCH = "project_activity_search";
    public static final String PROJECT_ACTIVITY_DETAIL = "project_activity_detail";

    public static final String PROJECT_REPORT_SPRINT = "project_report_sprint";
    public static final String PROJECT_REPORT_MEMBER = "project_report_member";
    public static final String PROJECT_REPORT_TIME = "project_report_time";

    public static final String BUG_DETAIL = "bug_detail";
    public static final String BUG_SEARCH = "bug_search";
    public static final String BUG_SUMMARY = "bug_summary";
    public static final String BUG_COMMENT_LIST = "bug_comment_list";
    public static final String BUG_EVIDENCE_LIST = "bug_evidence_list";
    public static final String BUG_ATTACHMENT_LIST = "bug_attachment_list";
    public static final String BUG_DASHBOARD = "bug_dashboard";
    public static final String BUG_REPORT = "bug_report";
    public static final String BUG_QA_METRICS = "bug_qa_metrics";

    // ===================== NOTIFICATION =====================

    public static final String NOTIFICATION_UNREAD_COUNT = "notification_unread_count";
    public static final String NOTIFICATION_LIST = "notification_list";

    // Legacy/specialized caches retained for existing modules. New cache usage
    // must still be declared here and have an explicit strategy/TTL.
    public static final String GLOBAL_SEARCH = "global_search";
    public static final String ATTACHMENT_LIST = "attachment_list";
    public static final String ATTACHMENT_USAGE = "attachment_usage";
    public static final String ATTACHMENT_SECURITY_SUMMARY = "attachment_security_summary";
    public static final String FILE_CLEANUP_RESULT = "file_cleanup_result";

    public static final String ANALYTICS_VELOCITY = "analytics_velocity";
    public static final String ANALYTICS_PROJECT_BURNUP = "analytics_project_burnup";
    public static final String ANALYTICS_SPRINT_BURNUP = "analytics_sprint_burnup";
    public static final String ANALYTICS_CUMULATIVE_FLOW = "analytics_cumulative_flow";
    public static final String ANALYTICS_SUMMARY = "analytics_summary";

    public static final String ADMIN_DASHBOARD = "admin_dashboard";
    public static final String ADMIN_AUDIT_SEARCH = "admin_audit_search";
    public static final String ADMIN_AUDIT_DETAIL = "admin_audit_detail";
    public static final String ADMIN_AUDIT_SUMMARY = "admin_audit_summary";
    public static final String ADMIN_IMPORT_AUDIT_SEARCH = "admin_import_audit_search";
    public static final String ADMIN_FILE_AUDIT_SEARCH = "admin_file_audit_search";
    public static final String ADMIN_USER_ACTIVITY_AUDIT = "admin_user_activity_audit";
}
