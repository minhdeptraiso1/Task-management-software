package com.project.taskmanagement.security;

import com.project.taskmanagement.config.JwtProperties;
import com.project.taskmanagement.integration.support.TestDataFactory;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthEndpointSecurityIT extends BaseSecurityIT {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void privateEndpoints_withoutToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/projects")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedAndExpiredAccessTokens_returnUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/users/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                )
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        get("/users/me")
                                .header(HttpHeaders.AUTHORIZATION, authorization(expiredAccessToken()))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401002))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    void loginEndpointIsPublic() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new LoginRequest(manager.getEmail(), TestDataFactory.DEFAULT_PASSWORD)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void actuatorHealthAndInfoArePublicButSensitiveEndpointsAreProtected() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.redis.status").value("UP"))
                .andExpect(jsonPath("$.components.fileStorage.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name")
                        .value("Task Management Agile Scrum Backend"))
                .andExpect(jsonPath("$.app.version").value("1.0.0"));

        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledUser_cannotUsePreviouslyIssuedAccessToken() throws Exception {
        mockMvc.perform(
                        patch("/admin/users/{userId}/disable", employee.getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/me")
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403002))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    void roleChange_revokesPreviouslyIssuedAccessToken() throws Exception {
        mockMvc.perform(
                        patch("/admin/users/{userId}/role", outsider.getId())
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\":\"MANAGER\"}")
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/me")
                                .header(HttpHeaders.AUTHORIZATION, authorization(outsiderToken))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminRoles_cannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(
                        get("/admin/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, authorization(managerToken))
                )
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        get("/admin/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, authorization(employeeToken))
                )
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        get("/admin/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, authorization(adminToken))
                )
                .andExpect(status().isOk());
    }

    private String expiredAccessToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(manager.getId().toString())
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(Date.from(now.minusSeconds(120)))
                .setExpiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(
                        jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
                ))
                .compact();
    }

    private record LoginRequest(String email, String password) {
    }
}
