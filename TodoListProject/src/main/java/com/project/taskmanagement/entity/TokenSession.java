package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "token_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenSession extends BaseIdEntity {

    @Column(
            name = "user_id",
            nullable = false
    )
    UUID userId;

    @Column(
            name = "refresh_token",
            nullable = false,
            columnDefinition = "TEXT"
    )
    String refreshTokenHash;

    @Column(
            name = "access_token_jti",
            length = 100
    )
    String accessTokenJti;

    @Column(
            name = "refresh_token_jti",
            length = 100
    )
    String refreshTokenJti;

    @Builder.Default
    @Column(
            name = "revoked",
            nullable = false
    )
    boolean revoked = false;

    @Column(name = "revoked_at")
    Instant revokedAt;

    @Column(
            name = "expired_at",
            nullable = false
    )
    Instant expiredAt;

    public void revoke(
            Instant revokedAt
    ) {
        this.revoked = true;
        this.revokedAt = revokedAt;
    }
}
