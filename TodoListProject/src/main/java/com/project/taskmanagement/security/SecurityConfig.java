package com.project.taskmanagement.security;

import com.project.taskmanagement.config.CorsProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

import static com.project.taskmanagement.security.SecurityEndpoints.PUBLIC;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SecurityConfig {

    JwtAuthenticationFilter jwtAuthenticationFilter;
    RestAuthenticationEntryPoint authenticationEntryPoint;
    RestAccessDeniedHandler accessDeniedHandler;
    CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // REST API dùng JWT nên không cần CSRF.
                .csrf(csrf -> csrf.disable())

                // Không lưu authentication trong HTTP session.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Không dùng trang login mặc định.
                .formLogin(form ->
                        form.disable()
                )

                // Không dùng Basic Authentication.
                .httpBasic(basic ->
                        basic.disable()
                )

                // Response JSON cho lỗi 401 và 403.
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                // Phân quyền URL.
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(PUBLIC)
                                .permitAll()

                                .requestMatchers("/admin/**")
                                .hasRole("ADMIN")

                                .anyRequest()
                                .authenticated()
                )

                // JWT filter phải chạy trước filter username/password.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOriginList());
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );
        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Content-Disposition",
                        "Content-Length"
                )
        );
        configuration.setAllowCredentials(
                corsProperties.allowCredentialsOrDefault()
        );
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }
}
