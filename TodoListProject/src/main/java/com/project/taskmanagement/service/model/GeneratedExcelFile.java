package com.project.taskmanagement.service.model;

public record GeneratedExcelFile(

        String fileName,

        String contentType,

        byte[] content

) {
}