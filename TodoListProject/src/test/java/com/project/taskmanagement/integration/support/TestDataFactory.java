package com.project.taskmanagement.integration.support;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class TestDataFactory {

    public static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public TestDataFactory(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public User createUser(
            String username,
            String email,
            UserRole role
    ) {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id, username, email, password, role, enabled,
                    created_at, updated_at, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, TRUE, ?, ?, ?, ?)
                """,
                userId,
                username,
                email,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                role.name(),
                Timestamp.from(now),
                Timestamp.from(now),
                "integration-test",
                "integration-test"
        );

        return userRepository.findById(userId)
                .orElseThrow();
    }
}
