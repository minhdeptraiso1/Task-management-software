package com.project.taskmanagement.repository.projection.attachment;

import java.util.UUID;

public interface DeletedAttachmentFileView {

    UUID getId();

    String getStoragePath();
}
