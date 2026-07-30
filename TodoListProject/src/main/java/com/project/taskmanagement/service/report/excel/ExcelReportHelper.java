package com.project.taskmanagement.service.report.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Component
public class ExcelReportHelper {

    public XSSFWorkbook createWorkbook() {
        return new XSSFWorkbook();
    }

    public CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);

        return style;
    }

    public CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    public CellStyle dateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
        return style;
    }

    public Row createHeaderRow(Sheet sheet, String... headers) {
        Row row = sheet.createRow(0);
        CellStyle style = headerStyle(sheet.getWorkbook());

        for (int index = 0; index < headers.length; index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(style);
        }

        sheet.createFreezePane(0, 1);
        return row;
    }

    public void setCell(Row row, int index, Object value) {
        Cell cell = row.createCell(index);

        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }

        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }

        if (value instanceof LocalDate localDate) {
            cell.setCellValue(localDate);
            cell.setCellStyle(dateStyle(row.getSheet().getWorkbook()));
            return;
        }

        if (value instanceof Instant instant) {
            cell.setCellValue(Date.from(instant));
            cell.setCellStyle(dateTimeStyle(row.getSheet().getWorkbook()));
            return;
        }

        cell.setCellValue(String.valueOf(value));
    }

    public void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    public void applyAutoFilter(Sheet sheet, int columnCount) {
        if (sheet.getPhysicalNumberOfRows() == 0 || columnCount <= 0) {
            return;
        }

        sheet.setAutoFilter(new CellRangeAddress(
                0,
                Math.max(0, sheet.getLastRowNum()),
                0,
                columnCount - 1
        ));
    }

    public String minutesToHourText(Number minutes) {
        if (minutes == null) {
            return "0h";
        }

        long totalMinutes = minutes.longValue();
        long hours = totalMinutes / 60;
        long remainMinutes = totalMinutes % 60;

        if (remainMinutes == 0) {
            return hours + "h";
        }

        return hours + "h " + remainMinutes + "m";
    }

    public String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "report.xlsx";
        }

        return fileName
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("\\s+", "-");
    }
}
