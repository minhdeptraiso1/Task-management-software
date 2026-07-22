package com.project.taskmanagement.dto.response.attachment;

import java.util.UUID;

public record AttachmentUsageResponse(
        UUID projectId,
        long usedBytes,
        long maxBytes,
        long remainingBytes,
        double usageRate
) {
}
