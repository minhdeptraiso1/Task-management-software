package com.project.taskmanagement.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.integration.support.AuthTestHelper;
import com.project.taskmanagement.integration.support.DatabaseCleaner;
import com.project.taskmanagement.testsupport.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@IntegrationTest
public abstract class BaseIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("task_management_integration_test")
                    .withUsername("test")
                    .withPassword("test");

    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine")
            ).withExposedPorts(6379);

    static {
        POSTGRESQL_CONTAINER.start();
        REDIS_CONTAINER.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @Autowired
    protected AuthTestHelper authTestHelper;

    @DynamicPropertySource
    static void overrideProperties(
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
        registry.add(
                "spring.data.redis.host",
                REDIS_CONTAINER::getHost
        );
        registry.add(
                "spring.data.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(6379)
        );
    }

    @BeforeEach
    protected void cleanTestState() {
        databaseCleaner.clean();
    }

    @AfterEach
    protected void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected UUID extractDataId(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        return UUID.fromString(
                root.path("data")
                        .path("id")
                        .asText()
        );
    }
}
