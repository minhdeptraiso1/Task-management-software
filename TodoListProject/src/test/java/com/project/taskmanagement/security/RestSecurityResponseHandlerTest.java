package com.project.taskmanagement.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class RestSecurityResponseHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void authenticationEntryPoint_shouldReturnConsistentUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RestAuthenticationEntryPoint(objectMapper).commence(
                request,
                response,
                new BadCredentialsException("invalid")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.get("code").asInt()).isEqualTo(401005);
        assertThat(body.get("path").asText()).isEqualTo("/projects");
        assertThat(body.hasNonNull("timestamp")).isTrue();
        assertThat(body.has("error")).isFalse();
    }

    @Test
    void accessDeniedHandler_shouldReturnConsistentForbiddenResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/projects/id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RestAccessDeniedHandler(objectMapper).handle(
                request,
                response,
                new AccessDeniedException("forbidden")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.get("code").asInt()).isEqualTo(403001);
        assertThat(body.get("path").asText()).isEqualTo("/projects/id");
        assertThat(body.hasNonNull("timestamp")).isTrue();
        assertThat(body.has("error")).isFalse();
    }
}
