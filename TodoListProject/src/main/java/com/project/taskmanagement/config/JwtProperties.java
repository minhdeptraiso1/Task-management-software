package com.project.taskmanagement.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProperties {

    @NotBlank
    String secret;

    @Min(5)
    long accessTokenExpirationMinutes = 30;

    @Min(1)
    long refreshTokenExpirationDays = 7;
}
