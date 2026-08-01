package com.project.taskmanagement.service.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuditRequestHelper {

    public String getClientIp(
            HttpServletRequest request
    ) {
        if (request == null) {
            return null;
        }

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {
            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }

    public String getUserAgent(
            HttpServletRequest request
    ) {
        if (request == null) {
            return null;
        }

        return request.getHeader("User-Agent");
    }
}
