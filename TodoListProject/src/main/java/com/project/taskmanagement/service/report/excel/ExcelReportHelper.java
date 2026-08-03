package com.project.taskmanagement.service.report.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Component
public class ExcelReportHelper {

    public XSSFWorkbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * Dùng cho báo cáo dữ liệu lớn, không có biểu đồ XSSF phức tạp.
     * Caller phải đóng workbook và gọi dispose sau khi ghi xong.
     */
    public SXSSFWorkbook createStreamingWorkbook() {
        return new SXSSFWorkbook(100);
    }

    public CellStyle headerStyle(Workbook workbook) {
        if (workbook instanceof XSSFWorkbook xssfWorkbook) {
            XSSFFont font = xssfWorkbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            font.setColor(new XSSFColor(new Color(255, 255, 255), null));

            XSSFCellStyle style = xssfWorkbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setFillForegroundColor(new XSSFColor(new Color(30, 41, 59), null)); // Dark Navy #1E293B
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.MEDIUM);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            return style;
        }

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
        style.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public CellStyle dateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public CellStyle statusStyle(Workbook workbook, String status) {
        if (workbook instanceof XSSFWorkbook xssfWorkbook && status != null) {
            XSSFFont font = xssfWorkbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 10);

            XSSFCellStyle style = xssfWorkbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            String s = status.toUpperCase();
            if (s.contains("DONE") || s.contains("COMPLETED") || s.contains("RESOLVED") || s.contains("CLOSED")) {
                style.setFillForegroundColor(new XSSFColor(new Color(220, 252, 231), null));
                font.setColor(new XSSFColor(new Color(22, 101, 52), null));
            } else if (s.contains("PROGRESS") || s.contains("REVIEW") || s.contains("ACTIVE")) {
                style.setFillForegroundColor(new XSSFColor(new Color(254, 243, 199), null));
                font.setColor(new XSSFColor(new Color(146, 64, 14), null));
            } else if (s.contains("BLOCKED") || s.contains("CANCELLED") || s.contains("CRITICAL")) {
                style.setFillForegroundColor(new XSSFColor(new Color(254, 226, 226), null));
                font.setColor(new XSSFColor(new Color(153, 27, 27), null));
            } else {
                style.setFillForegroundColor(new XSSFColor(new Color(241, 245, 249), null));
                font.setColor(new XSSFColor(new Color(51, 65, 85), null));
            }
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        }

        return workbook.createCellStyle();
    }

    public Row createHeaderRow(Sheet sheet, String... headers) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(28);
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

        String strValue = String.valueOf(value);
        cell.setCellValue(strValue);

        // Apply status style if enum status
        if (value instanceof Enum<?> || strValue.matches("(?i)^(DONE|COMPLETED|IN_PROGRESS|TODO|BLOCKED|CANCELLED|ACTIVE|CLOSED|RESOLVED|CRITICAL|HIGH|MEDIUM|LOW)$")) {
            cell.setCellStyle(statusStyle(row.getSheet().getWorkbook(), strValue));
        }
    }

    public void autoSizeColumns(Sheet sheet, int columnCount) {
        Row headerRow = sheet.getRow(0);

        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
            int currentWidth = sheet.getColumnWidth(index);
            String headerTitle = (headerRow != null && headerRow.getCell(index) != null)
                    ? headerRow.getCell(index).getStringCellValue()
                    : "";

            int minWidth = 4800; // ~18 chars default
            if (headerTitle.contains("Mã") || headerTitle.contains("ID") || headerTitle.contains("Email")
                    || headerTitle.contains("Tiêu đề") || headerTitle.contains("Mô tả") || headerTitle.contains("Tên")) {
                minWidth = 9000; // ~35 chars for IDs, Titles, Emails
            }

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(index, minWidth);
            } else {
                sheet.setColumnWidth(index, Math.min(currentWidth + 1400, 24000));
            }
        }
    }

    public void createVelocityChart(Sheet sheet, int lastRowIndex) {
        if (lastRowIndex <= 1 || !(sheet instanceof XSSFSheet xssfSheet)) {
            return;
        }

        try {
            XSSFDrawing drawing = xssfSheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, lastRowIndex + 2, 10, lastRowIndex + 18);
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Biểu đồ Velocity Sprint (Story Points Cam kết vs Hoàn thành)");
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

            XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 1, 1));
            XDDFNumericalDataSource<Double> ysCommitted = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 7, 7));
            XDDFNumericalDataSource<Double> ysCompleted = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 8, 8));

            XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
            data.setBarDirection(BarDirection.COL);

            XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) data.addSeries(xs, ysCommitted);
            series1.setTitle("Story Points Cam kết", null);

            XDDFBarChartData.Series series2 = (XDDFBarChartData.Series) data.addSeries(xs, ysCompleted);
            series2.setTitle("Story Points Hoàn thành", null);

            chart.plot(data);
        } catch (Exception ignored) {
            // Chart generation fallback
        }
    }

    public void createBurnupChart(Sheet sheet, int lastRowIndex) {
        if (lastRowIndex <= 1 || !(sheet instanceof XSSFSheet xssfSheet)) {
            return;
        }

        try {
            XSSFDrawing drawing = xssfSheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, lastRowIndex + 2, 8, lastRowIndex + 18);
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Biểu đồ Burnup Sprint (Tổng phạm vi vs Đã hoàn thành)");
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

            XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 0, 0));
            XDDFNumericalDataSource<Double> ysTotal = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 1, 1));
            XDDFNumericalDataSource<Double> ysCompleted = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 2, 2));

            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

            XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(xs, ysTotal);
            series1.setTitle("Tổng phạm vi", null);

            XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(xs, ysCompleted);
            series2.setTitle("Đã hoàn thành", null);

            chart.plot(data);
        } catch (Exception ignored) {
            // Chart generation fallback
        }
    }

    public void createMemberPerformanceChart(Sheet sheet, int lastRowIndex) {
        if (lastRowIndex <= 1 || !(sheet instanceof XSSFSheet xssfSheet)) {
            return;
        }

        try {
            XSSFDrawing drawing = xssfSheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, lastRowIndex + 2, 10, lastRowIndex + 18);
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Biểu đồ Hiệu suất Thành viên (Tổng số Task vs Task Hoàn thành)");
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

            XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 0, 0));
            XDDFNumericalDataSource<Double> ysTotal = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 2, 2));
            XDDFNumericalDataSource<Double> ysDone = XDDFDataSourcesFactory.fromNumericCellRange(xssfSheet, new CellRangeAddress(1, lastRowIndex - 1, 3, 3));

            XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
            data.setBarDirection(BarDirection.COL);

            XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) data.addSeries(xs, ysTotal);
            series1.setTitle("Tổng số Task", null);

            XDDFBarChartData.Series series2 = (XDDFBarChartData.Series) data.addSeries(xs, ysDone);
            series2.setTitle("Task Hoàn thành", null);

            chart.plot(data);
        } catch (Exception ignored) {
            // Chart generation fallback
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

