package com.project.taskmanagement.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsPropertiesTest {

    @Test
    void parsesAndDeduplicatesExplicitOrigins() {
        CorsProperties properties = new CorsProperties(
                "http://localhost:5173, https://app.hicas.vn, http://localhost:5173",
                true
        );

        assertEquals(
                List.of("http://localhost:5173", "https://app.hicas.vn"),
                properties.allowedOriginList()
        );
        assertTrue(properties.allowCredentialsOrDefault());
    }

    @Test
    void rejectsWildcardOrigin() {
        CorsProperties properties = new CorsProperties("*", false);

        assertThrows(
                IllegalStateException.class,
                properties::allowedOriginList
        );
    }

    @Test
    void credentialsAreDisabledByDefault() {
        CorsProperties properties = new CorsProperties(null, null);

        assertEquals(
                List.of("http://localhost:5173"),
                properties.allowedOriginList()
        );
        assertFalse(properties.allowCredentialsOrDefault());
    }
}
