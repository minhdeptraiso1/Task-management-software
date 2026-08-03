package com.project.taskmanagement.security.websocket;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.repository.TokenSessionRepository;
import com.project.taskmanagement.security.JwtTokenProvider;
import com.project.taskmanagement.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class JwtHandshakeInterceptor
        implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE =
            "WS_USER_ID";

    JwtTokenProvider jwtTokenProvider;
    TokenBlacklistService tokenBlacklistService;
    UserRepository userRepository;
    TokenSessionRepository tokenSessionRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token =
                resolveToken(request);

        if (token == null
                || token.isBlank()) {
            return false;
        }

        try {
            if (tokenBlacklistService.isRevoked(token)) {
                return false;
            }

            Claims claims =
                    jwtTokenProvider.getClaims(token);

            UUID userId =
                    jwtTokenProvider.getUserId(token);

            String accessTokenJti = claims.getId();

            if (accessTokenJti == null
                    || accessTokenJti.isBlank()
                    || !tokenSessionRepository
                    .existsByUserIdAndAccessTokenJtiAndRevokedFalse(
                            userId,
                            accessTokenJti
                    )) {
                return false;
            }

            User user =
                    userRepository
                            .findById(userId)
                            .orElse(null);

            if (user == null
                    || !user.isEnabled()
                    || isIssuedBeforeLogoutAll(claims, user)) {
                return false;
            }

            attributes.put(
                    USER_ID_ATTRIBUTE,
                    userId.toString()
            );

            return true;
        } catch (JwtException
                 | IllegalArgumentException exception) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No-op.
    }

    private String resolveToken(
            ServerHttpRequest request
    ) {
        List<String> authorizationHeaders =
                request.getHeaders()
                        .get("Authorization");

        if (authorizationHeaders != null
                && !authorizationHeaders.isEmpty()) {

            String bearerToken =
                    authorizationHeaders.get(0);

            if (bearerToken != null
                    && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }

        var queryParams =
                UriComponentsBuilder
                        .fromUri(request.getURI())
                        .build()
                        .getQueryParams();

        String token =
                queryParams.getFirst("token");

        if (token != null
                && !token.isBlank()) {
            return token;
        }

        return queryParams.getFirst("access_token");
    }

    private boolean isIssuedBeforeLogoutAll(
            Claims claims,
            User user
    ) {
        if (user.getLogoutAllAt() == null) {
            return false;
        }

        if (claims.getIssuedAt() == null) {
            return true;
        }

        Instant issuedAt =
                claims.getIssuedAt()
                        .toInstant();

        return issuedAt.isBefore(
                user.getLogoutAllAt()
        );
    }
}
