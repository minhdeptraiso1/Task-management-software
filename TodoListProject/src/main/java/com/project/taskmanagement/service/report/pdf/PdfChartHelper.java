package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.pdf.PdfPTable;
import com.project.taskmanagement.dto.response.taskstatistics.BurndownPointResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfChartHelper {

    PdfTableHelper pdfTableHelper;
    PdfReportHelper pdfReportHelper;

    public PdfPTable burndownDataTable(List<BurndownPointResponse> points) {
        PdfPTable table = pdfTableHelper.fullWidthTable(5);
        table.addCell(pdfTableHelper.headerCell("Date"));
        table.addCell(pdfTableHelper.headerCell("Ideal Remaining"));
        table.addCell(pdfTableHelper.headerCell("Actual Remaining"));
        table.addCell(pdfTableHelper.headerCell("Completed Total"));
        table.addCell(pdfTableHelper.headerCell("Completed On Date"));

        if (points == null) {
            return table;
        }

        for (BurndownPointResponse point : points) {
            table.addCell(pdfTableHelper.textCell(pdfReportHelper.format(point.date())));
            table.addCell(pdfTableHelper.textCell(point.idealRemainingTasks()));
            table.addCell(pdfTableHelper.textCell(point.actualRemainingTasks()));
            table.addCell(pdfTableHelper.textCell(point.completedTasks()));
            table.addCell(pdfTableHelper.textCell(point.completedOnDate()));
        }

        return table;
    }
}
