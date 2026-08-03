package com.project.taskmanagement.repository.support;

import com.project.taskmanagement.testsupport.RepositoryTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@RepositoryTest
public abstract class RepositoryTestBase {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("task_management_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    @BeforeEach
    void setRepositoryTestAuditor() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        "repository-test",
                                        null,
                                        java.util.List.of()
                                )
                );
    }

    @AfterEach
    void clearRepositoryTestAuditor() {
        SecurityContextHolder.clearContext();
    }

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRESQL_CONTAINER::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRESQL_CONTAINER::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRESQL_CONTAINER::getPassword
        );
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
    }
}
