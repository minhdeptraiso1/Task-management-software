package com.project.taskmanagement.service;

import com.project.taskmanagement.service.model.GeneratedExcelFile;

import java.util.UUID;

public interface TaskExcelTemplateService {

    GeneratedExcelFile generateTemplate(
            UUID projectId,
            UUID sprintId
    );
}