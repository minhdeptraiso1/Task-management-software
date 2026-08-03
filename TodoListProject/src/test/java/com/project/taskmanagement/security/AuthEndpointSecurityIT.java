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
                .andExpect(jsonPath("$.error.code").value("401002"));
    }

    @Test
    void loginAndOpenApiEndpoints_arePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new LoginRequest(manager.getEmail(), TestDataFactory.DEFAULT_PASSWORD)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
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
                .andExpect(jsonPath("$.error.code").value("403002"));
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
