package com.project.taskmanagement.integration.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    private final RedisConnectionFactory redisConnectionFactory;

    public DatabaseCleaner(
            RedisConnectionFactory redisConnectionFactory
    ) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Transactional
    public void clean() {
        @SuppressWarnings("unchecked")
        List<String> tables = entityManager
                .createNativeQuery(
                        """
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public'
                          AND tablename <> 'flyway_schema_history'
                        """,
                        String.class
                )
                .getResultList();

        if (!tables.isEmpty()) {
            String tableNames = tables.stream()
                    .map(DatabaseCleaner::quoteIdentifier)
                    .reduce((left, right) -> left + ", " + right)
                    .orElseThrow();

            entityManager.createNativeQuery(
                    "TRUNCATE TABLE " + tableNames
                            + " RESTART IDENTITY CASCADE"
            ).executeUpdate();
        }

        try (RedisConnection connection =
                     redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
