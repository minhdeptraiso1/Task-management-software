package com.project.taskmanagement.meeting.validator;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

@Component
public class GoogleMeetLinkValidator {
    private static final Pattern CODE = Pattern.compile("^[a-z]{3}-[a-z]{4}-[a-z]{3}$");

    public String normalizeAndValidate(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        try {
            URI u = URI.create(v);
            if (!"https".equalsIgnoreCase(u.getScheme()) || !"meet.google.com".equalsIgnoreCase(u.getHost()) || u.getPath() == null || !CODE.matcher(u.getPath().substring(1)).matches())
                throw new Exception();
            return v;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Link Google Meet không hợp lệ");
        }
    }
}
