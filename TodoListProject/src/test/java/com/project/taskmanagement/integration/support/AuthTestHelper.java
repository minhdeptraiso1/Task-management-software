package com.project.taskmanagement.integration.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class AuthTestHelper {

    private final ObjectMapper objectMapper;

    public AuthTestHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String loginAndGetAccessToken(
            MockMvc mockMvc,
            String email,
            String password
    ) throws Exception {
        return loginAndGetAccessToken(
                mockMvc,
                email,
                password,
                "127.0.0.1"
        );
    }

    public String loginAndGetAccessToken(
            MockMvc mockMvc,
            String email,
            String password,
            String remoteAddress
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/auth/login")
                                .with(request -> {
                                    request.setRemoteAddr(remoteAddress);
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new LoginBody(
                                                        email,
                                                        password
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data")
                .path("accessToken")
                .asText();
    }

    public String bearer(String token) {
        return "Bearer " + token;
    }

    private record LoginBody(
            String email,
            String password
    ) {
    }
}
