package com.project.taskmanagement.security.websocket;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.TokenSessionRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.security.JwtTokenProvider;
import com.project.taskmanagement.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtHandshakeInterceptorTest {

    private JwtTokenProvider jwtTokenProvider;
    private TokenBlacklistService tokenBlacklistService;
    private UserRepository userRepository;
    private TokenSessionRepository tokenSessionRepository;
    private JwtHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        userRepository = mock(UserRepository.class);
        tokenSessionRepository = mock(TokenSessionRepository.class);
        interceptor = new JwtHandshakeInterceptor(
                jwtTokenProvider,
                tokenBlacklistService,
                userRepository,
                tokenSessionRepository
        );
    }

    @Test
    void acceptsOnlyActiveAccessTokenSession() {
        String token = "access-token";
        UUID userId = UUID.randomUUID();
        String accessTokenJti = UUID.randomUUID().toString();
        Claims claims = mock(Claims.class);
        User user = mock(User.class);

        when(tokenBlacklistService.isRevoked(token)).thenReturn(false);
        when(jwtTokenProvider.getClaims(token)).thenReturn(claims);
        when(jwtTokenProvider.getUserId(token)).thenReturn(userId);
        when(claims.getId()).thenReturn(accessTokenJti);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.isEnabled()).thenReturn(true);
        when(user.getLogoutAllAt()).thenReturn(null);
        when(tokenSessionRepository.existsByUserIdAndAccessTokenJtiAndRevokedFalse(
                userId,
                accessTokenJti
        )).thenReturn(true);

        Map<String, Object> attributes = new HashMap<>();

        assertTrue(handshake(token, attributes));
        assertEquals(
                userId.toString(),
                attributes.get(JwtHandshakeInterceptor.USER_ID_ATTRIBUTE)
        );
    }

    @Test
    void rejectsRevokedOrMissingAccessTokenSession() {
        String token = "access-token";
        UUID userId = UUID.randomUUID();
        String accessTokenJti = UUID.randomUUID().toString();
        Claims claims = mock(Claims.class);

        when(tokenBlacklistService.isRevoked(token)).thenReturn(false);
        when(jwtTokenProvider.getClaims(token)).thenReturn(claims);
        when(jwtTokenProvider.getUserId(token)).thenReturn(userId);
        when(claims.getId()).thenReturn(accessTokenJti);
        when(tokenSessionRepository.existsByUserIdAndAccessTokenJtiAndRevokedFalse(
                userId,
                accessTokenJti
        )).thenReturn(false);

        assertFalse(handshake(token, new HashMap<>()));
    }

    private boolean handshake(
            String token,
            Map<String, Object> attributes
    ) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/ws");
        servletRequest.setQueryString("access_token=" + token);
        servletRequest.setParameter("access_token", token);

        return interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class),
                attributes
        );
    }
}
