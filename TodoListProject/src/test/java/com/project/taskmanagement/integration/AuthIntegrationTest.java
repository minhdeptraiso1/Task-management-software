package com.project.taskmanagement.integration;

import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.integration.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid()
            throws Exception {
        testDataFactory.createUser(
                "manager",
                "manager.login@test.com",
                UserRole.MANAGER
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "manager.login@test.com",
                                          "password": "123456"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsInvalid()
            throws Exception {
        testDataFactory.createUser(
                "manager",
                "manager.invalid@test.com",
                UserRole.MANAGER
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "manager.invalid@test.com",
                                          "password": "wrong-password"
                                        }
                                        """
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists());
    }
}
