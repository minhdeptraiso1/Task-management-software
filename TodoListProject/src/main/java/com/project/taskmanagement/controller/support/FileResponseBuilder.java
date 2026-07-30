package com.project.taskmanagement.controller.support;

import com.project.taskmanagement.service.model.GeneratedReportFile;
import com.project.taskmanagement.util.DownloadHeaderUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileResponseBuilder {

    public ResponseEntity<byte[]> build(GeneratedReportFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        DownloadHeaderUtils.attachmentContentDisposition(file.fileName())
                )
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(file.content());
    }
}
