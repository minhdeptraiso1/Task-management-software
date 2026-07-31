package com.project.taskmanagement.security.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeHandler
        extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object userId =
                attributes.get(
                        JwtHandshakeInterceptor.USER_ID_ATTRIBUTE
                );

        if (userId == null) {
            return null;
        }

        return new WebSocketUserPrincipal(
                userId.toString()
        );
    }
}
