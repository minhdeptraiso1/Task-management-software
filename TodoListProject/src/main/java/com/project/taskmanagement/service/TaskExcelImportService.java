package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.taskimport.TaskImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TaskExcelImportService {

    TaskImportResponse importTasks(
            UUID projectId,
            UUID sprintId,
            MultipartFile file
    );
}