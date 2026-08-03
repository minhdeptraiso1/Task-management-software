package com.project.taskmanagement.service.cache;

import com.project.taskmanagement.config.CacheNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheEvictServiceImplTest {

    private ConcurrentMapCacheManager cacheManager;
    private CacheEvictService cacheEvictService;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager();
        cacheEvictService = new CacheEvictServiceImpl(cacheManager);
    }

    @Test
    void evictProjectWorkspaceClearsProjectCachesOnly() {
        put(CacheNames.PROJECT_DETAIL, "member-a:project-1", "stale");
        put(CacheNames.PROJECT_DASHBOARD, "member-a:project-1", "stale");
        put(CacheNames.PROJECT_SEARCH, "member-a:page-0", "stale");
        put(CacheNames.NOTIFICATION_UNREAD_COUNT, "member-a", 2L);

        cacheEvictService.evictProjectWorkspace(UUID.randomUUID());

        assertThat(get(CacheNames.PROJECT_DETAIL, "member-a:project-1")).isNull();
        assertThat(get(CacheNames.PROJECT_DASHBOARD, "member-a:project-1")).isNull();
        assertThat(get(CacheNames.PROJECT_SEARCH, "member-a:page-0")).isNull();
        assertThat(get(CacheNames.NOTIFICATION_UNREAD_COUNT, "member-a"))
                .isEqualTo(2L);
    }

    @Test
    void evictSprintWorkspaceClearsSprintDerivedCachesWithoutMembers() {
        put(CacheNames.SPRINT_KANBAN, "member-a:project-1:sprint-1", "stale");
        put(CacheNames.SPRINT_TASK_STATISTICS, "member-a:sprint-1", "stale");
        put(CacheNames.SPRINT_BURNDOWN, "member-a:sprint-1", "stale");
        put(CacheNames.PROJECT_MEMBERS, "member-a:project-1", "keep");

        cacheEvictService.evictSprintWorkspace(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        assertThat(get(CacheNames.SPRINT_KANBAN, "member-a:project-1:sprint-1")).isNull();
        assertThat(get(CacheNames.SPRINT_TASK_STATISTICS, "member-a:sprint-1")).isNull();
        assertThat(get(CacheNames.SPRINT_BURNDOWN, "member-a:sprint-1")).isNull();
        assertThat(get(CacheNames.PROJECT_MEMBERS, "member-a:project-1"))
                .isEqualTo("keep");
    }

    @Test
    void evictNotificationWorkspaceClearsListCountAndDashboard() {
        put(CacheNames.NOTIFICATION_LIST, "member-a:page-0", "stale");
        put(CacheNames.NOTIFICATION_UNREAD_COUNT, "member-a", 3L);
        put(CacheNames.MY_DASHBOARD, "member-a", "stale");
        put(CacheNames.PROJECT_MEMBERS, "member-a:project-1", "keep");

        cacheEvictService.evictNotificationWorkspace(UUID.randomUUID());

        assertThat(get(CacheNames.NOTIFICATION_LIST, "member-a:page-0")).isNull();
        assertThat(get(CacheNames.NOTIFICATION_UNREAD_COUNT, "member-a")).isNull();
        assertThat(get(CacheNames.MY_DASHBOARD, "member-a")).isNull();
        assertThat(get(CacheNames.PROJECT_MEMBERS, "member-a:project-1"))
                .isEqualTo("keep");
    }

    @Test
    void redisFailureDoesNotBreakBusinessInvalidationFlow() {
        CacheManager failingCacheManager = mock(CacheManager.class);
        when(failingCacheManager.getCache(CacheNames.PROJECT_DETAIL))
                .thenThrow(new IllegalStateException("Redis unavailable"));

        CacheEvictService failOpenService =
                new CacheEvictServiceImpl(failingCacheManager);

        assertThatCode(() ->
                failOpenService.evictProjectWorkspace(UUID.randomUUID())
        ).doesNotThrowAnyException();
    }

    private void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();
        cache.put(key, value);
    }

    private Object get(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();
        return cache.get(key, Object.class);
    }
}
