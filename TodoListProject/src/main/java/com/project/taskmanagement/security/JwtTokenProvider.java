package com.project.taskmanagement.security;

import com.project.taskmanagement.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM =
            "tokenType";

    private static final String ACCESS_TOKEN_TYPE =
            "ACCESS";

    private static final String REFRESH_TOKEN_TYPE =
            "REFRESH";

    Key key;
    JwtProperties jwtProperties;

    public JwtTokenProvider(
            JwtProperties jwtProperties
    ) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    // ===================== ACCESS TOKEN =====================

    public String generateAccessToken(
            UUID userId,
            String username,
            String role,
            String jti
    ) {
        Instant now = Instant.now();

        Instant expiry =
                now.plus(
                        jwtProperties.getAccessTokenExpirationMinutes(),
                        ChronoUnit.MINUTES
                );

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .setId(jti)
                .claim(
                        TOKEN_TYPE_CLAIM,
                        ACCESS_TOKEN_TYPE
                )
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // ===================== REFRESH TOKEN =====================

    public String generateRefreshToken(
            UUID sessionId,
            String jti
    ) {
        Instant now = Instant.now();

        Instant expiry =
                now.plus(
                        jwtProperties.getRefreshTokenExpirationDays(),
                        ChronoUnit.DAYS
                );

        return Jwts.builder()
                .setSubject(sessionId.toString())
                .setId(jti)
                .claim(
                        TOKEN_TYPE_CLAIM,
                        REFRESH_TOKEN_TYPE
                )
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // ===================== VALIDATE =====================

    public Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Claims getClaims(String token) {
        return validateToken(token).getBody();
    }

    // ===================== ACCESS TOKEN HELPERS =====================

    public UUID getUserId(String token) {
        Claims claims = getClaims(token);

        validateTokenType(
                claims,
                ACCESS_TOKEN_TYPE
        );

        return UUID.fromString(
                claims.getSubject()
        );
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);

        validateTokenType(
                claims,
                ACCESS_TOKEN_TYPE
        );

        return claims.get(
                "username",
                String.class
        );
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);

        validateTokenType(
                claims,
                ACCESS_TOKEN_TYPE
        );

        return claims.get(
                "role",
                String.class
        );
    }

    public String getJti(String token) {
        return getClaims(token)
                .getId();
    }

    // ===================== REFRESH TOKEN HELPERS =====================

    public UUID getSessionIdFromRefreshToken(
            String refreshToken
    ) {
        Claims claims = getClaims(refreshToken);

        validateTokenType(
                claims,
                REFRESH_TOKEN_TYPE
        );

        return UUID.fromString(
                claims.getSubject()
        );
    }

    // ===================== COMMON HELPERS =====================

    public java.time.Duration getRemainingDuration(String token) {
        Date expiration = getClaims(token)
                .getExpiration();

        long remainingMillis =
                expiration.getTime()
                        - Instant.now().toEpochMilli();

        if (remainingMillis <= 0) {
            return java.time.Duration.ZERO;
        }

        return java.time.Duration.ofMillis(
                remainingMillis
        );
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now()
                .plus(
                        jwtProperties.getRefreshTokenExpirationDays(),
                        ChronoUnit.DAYS
                );
    }

    private void validateTokenType(
            Claims claims,
            String expectedType
    ) {
        String actualType = claims.get(
                TOKEN_TYPE_CLAIM,
                String.class
        );

        if (!expectedType.equals(actualType)) {
            throw new IllegalArgumentException(
                    "Invalid token type"
            );
        }
    }
}
