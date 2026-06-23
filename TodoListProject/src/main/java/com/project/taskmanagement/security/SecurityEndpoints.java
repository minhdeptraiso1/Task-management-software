package com.project.taskmanagement.security;

public final class SecurityEndpoints {

    private SecurityEndpoints() {
    }

    public static final String[] PUBLIC = {
            "/auth/login",
            "/auth/refresh",

            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",

            "/actuator/health"
    };
}