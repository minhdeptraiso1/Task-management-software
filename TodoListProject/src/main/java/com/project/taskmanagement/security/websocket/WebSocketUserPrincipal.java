package com.project.taskmanagement.security.websocket;

import java.security.Principal;

public record WebSocketUserPrincipal(

        String name
) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
