package com.project.taskmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
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
    long accessTokenExpiration;
    long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,

            @Value("${jwt.access-token-expiration}")
            long accessTokenExpiration,

            @Value("${jwt.refresh-token-expiration}")
            long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTokenExpiration =
                accessTokenExpiration * 1000;

        this.refreshTokenExpiration =
                refreshTokenExpiration * 1000;
    }

    // ===================== ACCESS TOKEN =====================

    public String generateAccessToken(
            UUID userId,
            String username,
            String role
    ) {
        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .claim(
                        TOKEN_TYPE_CLAIM,
                        ACCESS_TOKEN_TYPE
                )
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // ===================== REFRESH TOKEN =====================

    public String generateRefreshToken(UUID sessionId) {
        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + refreshTokenExpiration
        );

        return Jwts.builder()
                .setSubject(sessionId.toString())
                .claim(
                        TOKEN_TYPE_CLAIM,
                        REFRESH_TOKEN_TYPE
                )
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
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

    public Duration getRemainingDuration(String token) {
        Date expiration = getClaims(token)
                .getExpiration();

        long remainingMillis =
                expiration.getTime()
                        - Instant.now().toEpochMilli();

        if (remainingMillis <= 0) {
            return Duration.ZERO;
        }

        return Duration.ofMillis(
                remainingMillis
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