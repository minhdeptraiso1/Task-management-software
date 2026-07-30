package com.project.taskmanagement.service.report;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ReportFileNameBuilder {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public String sprintExcel(Project project, Sprint sprint) {
        return normalize("sprint-report-" + project.getCode() + "-" + sprint.getName() + "-" + today() + ".xlsx");
    }

    public String projectExcel(Project project) {
        return normalize("project-report-" + project.getCode() + "-" + today() + ".xlsx");
    }

    public String sprintPdf(Project project, Sprint sprint) {
        return normalize("sprint-report-" + project.getCode() + "-" + sprint.getName() + "-" + today() + ".pdf");
    }

    public String projectPdf(Project project) {
        return normalize("project-report-" + project.getCode() + "-" + today() + ".pdf");
    }

    private String today() {
        return LocalDate.now(BUSINESS_ZONE).toString();
    }

    private String normalize(String value) {
        return value
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-")
                .toLowerCase();
    }
}
