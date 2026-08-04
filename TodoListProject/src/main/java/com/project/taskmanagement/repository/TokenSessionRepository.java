package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenSessionRepository
        extends JpaRepository<TokenSession, UUID> {

    Optional<TokenSession> findByRefreshTokenHashAndRevokedFalse(
            String refreshTokenHash
    );

    Optional<TokenSession> findByIdAndRefreshTokenHashAndRevokedFalse(
            UUID id,
            String refreshTokenHash
    );

    boolean existsByUserIdAndAccessTokenJtiAndRevokedFalse(
            UUID userId,
            String accessTokenJti
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE TokenSession t
            SET t.revoked = true
                , t.revokedAt = :revokedAt
            WHERE t.userId = :userId
              AND t.revoked = false
            """)
    int revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("revokedAt") Instant revokedAt
    );
}
