package com.project.taskmanagement.service.cache;

import com.project.taskmanagement.config.CacheNames;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheEvictServiceImpl implements CacheEvictService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CacheEvictServiceImpl.class);

    CacheManager cacheManager;

    @Override
    public void evictProjectWorkspace(UUID projectId) {
        // Detail/dashboard keys are user-scoped, therefore a bare project UUID
        // cannot evict them safely. Clear only the affected cache regions.
        clear(CacheNames.PROJECT_DETAIL);
        clear(CacheNames.PROJECT_SEARCH);
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_REPORT_MEMBER);
        clear(CacheNames.PROJECT_REPORT_TIME);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
        clear(CacheNames.GLOBAL_SEARCH);
    }

    @Override
    public void evictProjectMembers(UUID projectId) {
        clear(CacheNames.PROJECT_MEMBERS);
        clear(CacheNames.USER_PROJECT_CANDIDATE_SEARCH);
        clear(CacheNames.BACKLOG_ITEM_DETAIL);
        clear(CacheNames.BACKLOG_ITEM_SEARCH);
        clear(CacheNames.SPRINT_DETAIL);
        clear(CacheNames.SPRINT_SEARCH);
        clear(CacheNames.TASK_DETAIL);
        clear(CacheNames.TASK_SEARCH);
        clear(CacheNames.TASK_COMMENT_LIST);
        clear(CacheNames.TASK_TIME_LOG_LIST);
        clear(CacheNames.TASK_TIME_SUMMARY);
        clearSprintDerivedCaches();
        clear(CacheNames.MY_DASHBOARD);
        clear(CacheNames.MY_TASK_SEARCH);
        clear(CacheNames.MY_TIME_SUMMARY);
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_REPORT_MEMBER);
        clear(CacheNames.PROJECT_REPORT_TIME);
        clear(CacheNames.PROJECT_SEARCH);
    }

    @Override
    public void evictSprintWorkspace(UUID projectId, UUID sprintId) {
        evict(CacheNames.CURRENT_SPRINT, projectId);
        clear(CacheNames.SPRINT_DETAIL);
        clear(CacheNames.SPRINT_LIST);
        clearSprintDerivedCaches();
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_REPORT_MEMBER);
        clear(CacheNames.PROJECT_REPORT_TIME);
        clear(CacheNames.ANALYTICS_VELOCITY);
        clear(CacheNames.ANALYTICS_PROJECT_BURNUP);
        clear(CacheNames.ANALYTICS_SPRINT_BURNUP);
        clear(CacheNames.ANALYTICS_CUMULATIVE_FLOW);
        clear(CacheNames.ANALYTICS_SUMMARY);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
    }

    @Override
    public void evictBacklogWorkspace(UUID projectId, UUID sprintId) {
        clear(CacheNames.BACKLOG_ITEM_DETAIL);
        clear(CacheNames.BACKLOG_LIST);
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
        if (sprintId != null) {
            clearSprintDerivedCaches();
        }
    }

    @Override
    public void evictTaskWorkspace(
            UUID projectId,
            UUID sprintId,
            UUID taskId,
            UUID assigneeUserId
    ) {
        clear(CacheNames.TASK_DETAIL);
        clear(CacheNames.TASK_SEARCH);
        clear(CacheNames.MY_TASK_SEARCH);
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_REPORT_MEMBER);
        clear(CacheNames.PROJECT_REPORT_TIME);
        clear(CacheNames.GLOBAL_SEARCH);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
        clear(CacheNames.ANALYTICS_SUMMARY);
        if (assigneeUserId != null) {
            clear(CacheNames.MY_DASHBOARD);
            clear(CacheNames.MY_TIME_SUMMARY);
        }
        if (sprintId != null) {
            clearSprintDerivedCaches();
            clear(CacheNames.ANALYTICS_SPRINT_BURNUP);
            clear(CacheNames.ANALYTICS_CUMULATIVE_FLOW);
        }
    }

    @Override
    public void evictTaskComments(UUID projectId, UUID taskId) {
        clear(CacheNames.TASK_COMMENT_LIST);
        clear(CacheNames.TASK_DETAIL);
        clear(CacheNames.GLOBAL_SEARCH);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
    }

    @Override
    public void evictTaskTimeLogs(
            UUID projectId,
            UUID sprintId,
            UUID taskId,
            UUID userId
    ) {
        clear(CacheNames.TASK_TIME_LOG_LIST);
        clear(CacheNames.TASK_TIME_SUMMARY);
        clear(CacheNames.TASK_DETAIL);
        clear(CacheNames.TASK_SEARCH);
        clear(CacheNames.MY_DASHBOARD);
        clear(CacheNames.MY_TASK_SEARCH);
        clear(CacheNames.MY_TIME_SUMMARY);
        clear(CacheNames.PROJECT_DASHBOARD);
        clear(CacheNames.PROJECT_DASHBOARD_WORKLOAD);
        clear(CacheNames.PROJECT_REPORT_SPRINT);
        clear(CacheNames.PROJECT_REPORT_MEMBER);
        clear(CacheNames.PROJECT_REPORT_TIME);
        if (sprintId != null) {
            clear(CacheNames.SPRINT_TASK_STATISTICS);
            clear(CacheNames.SPRINT_BURNDOWN);
            clear(CacheNames.SPRINT_PROGRESS);
            clear(CacheNames.ANALYTICS_SPRINT_BURNUP);
            clear(CacheNames.ANALYTICS_CUMULATIVE_FLOW);
        }
    }

    @Override
    public void evictNotificationWorkspace(UUID userId) {
        clear(CacheNames.NOTIFICATION_LIST);
        clear(CacheNames.NOTIFICATION_UNREAD_COUNT);
        clear(CacheNames.MY_DASHBOARD);
    }

    @Override
    public void evictSearchWorkspace(UUID projectId) {
        clear(CacheNames.GLOBAL_SEARCH);
        clear(CacheNames.PROJECT_SEARCH_RESULT);
        clear(CacheNames.PROJECT_SEARCH);
    }

    private void clearSprintDerivedCaches() {
        clear(CacheNames.SPRINT_KANBAN);
        clear(CacheNames.SPRINT_TASK_STATISTICS);
        clear(CacheNames.SPRINT_BURNDOWN);
        clear(CacheNames.SPRINT_CAPACITY);
        clear(CacheNames.SPRINT_HEALTH);
        clear(CacheNames.SPRINT_RISKS);
        clear(CacheNames.SPRINT_PROGRESS);
        clear(CacheNames.SPRINT_CLOSING_REPORT);
        clear(CacheNames.SPRINT_REVIEW);
        clear(CacheNames.SPRINT_RETROSPECTIVE);
    }

    private void evict(String cacheName, Object key) {
        if (cacheName == null || key == null) {
            return;
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (RuntimeException exception) {
            logFailure("evict", cacheName, key, exception);
        }
    }

    private void clear(String cacheName) {
        if (cacheName == null) {
            return;
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (RuntimeException exception) {
            logFailure("clear", cacheName, null, exception);
        }
    }

    private void logFailure(
            String operation,
            String cacheName,
            Object key,
            RuntimeException exception
    ) {
        LOGGER.warn(
                "Redis cache {} failed; continuing business transaction. cache={}, key={}, reason={}",
                operation,
                cacheName,
                key,
                exception.getMessage()
        );
    }
}
