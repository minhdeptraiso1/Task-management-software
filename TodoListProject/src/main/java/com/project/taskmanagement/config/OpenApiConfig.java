package com.project.taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI taskManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management Agile/Scrum API")
                        .version("1.0.0")
                        .description("API backend cho hệ thống quản lý công việc văn phòng IT theo Agile/Scrum")
                        .contact(new Contact()
                                .name("HICAS Task Management Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Academic Project")))
                .servers(List.of(
                        new Server()
                                .url("/api")
                                .description("API base path mặc định"),
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development")
                ))
                .addSecurityItem(
                        new SecurityRequirement().addList(SECURITY_SCHEME_NAME)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Nhập JWT access token. Swagger tự thêm tiền tố Bearer.")
                                )
                );
    }
}
