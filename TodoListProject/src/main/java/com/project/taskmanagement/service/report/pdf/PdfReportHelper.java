package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfReportHelper {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    final PdfReportTheme theme;
    final ResourceLoader resourceLoader;

    @Value("${app.report.pdf.font-path:fonts/NotoSans-Regular.ttf}")
    String fontPath;

    @Value("${app.report.pdf.bold-font-path:fonts/NotoSans-Bold.ttf}")
    String boldFontPath;

    public Document createDocument() {
        return new Document(
                PageSize.A4,
                theme.getPageMargin(),
                theme.getPageMargin(),
                theme.getPageMargin() + 12,
                theme.getPageMargin() + 12
        );
    }

    public Paragraph title(String text) {
        Font font = boldFont(theme.getTitleFontSize());
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(16);
        return paragraph;
    }

    public Paragraph sectionTitle(String text) {
        Font font = boldFont(theme.getHeadingFontSize());
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    public Paragraph normalText(String text) {
        Font font = regularFont(theme.getNormalFontSize());
        Paragraph paragraph = new Paragraph(text == null ? "" : text, font);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    public Paragraph generatedAt() {
        Font font = regularFont(theme.getSmallFontSize());
        Paragraph paragraph = new Paragraph(
                "Generated at: " + LocalDateTime.now(BUSINESS_ZONE).format(DATE_TIME_FORMATTER),
                font
        );
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    public PdfPTable keyValueTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    public PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    public void addHeaderCell(PdfPTable table, String value) {
        Font font = boldFont(theme.getSmallFontSize());
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(theme.getTableCellPadding());
        table.addCell(cell);
    }

    public void addCell(PdfPTable table, Object value) {
        Font font = regularFont(theme.getSmallFontSize());
        PdfPCell cell = new PdfPCell(new Phrase(format(value), font));
        cell.setPadding(theme.getTableCellPadding());
        table.addCell(cell);
    }

    public void addKeyValueRow(PdfPTable table, String key, Object value) {
        addHeaderCell(table, key);
        addCell(table, value);
    }

    public String minutesToHourText(Number minutes) {
        if (minutes == null) {
            return "0h";
        }

        long totalMinutes = minutes.longValue();
        long hours = totalMinutes / 60;
        long remainMinutes = totalMinutes % 60;
        return remainMinutes == 0 ? hours + "h" : hours + "h " + remainMinutes + "m";
    }

    public String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "report.pdf";
        }

        return fileName
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("\\s+", "-");
    }

    public String format(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof LocalDate localDate) {
            return DATE_FORMATTER.format(localDate);
        }

        if (value instanceof Instant instant) {
            return DATE_TIME_FORMATTER.format(instant.atZone(BUSINESS_ZONE));
        }

        return String.valueOf(value);
    }

    private Font regularFont(float size) {
        BaseFont baseFont = loadBaseFont(fontPath);
        return baseFont == null
                ? FontFactory.getFont(FontFactory.HELVETICA, size)
                : new Font(baseFont, size);
    }

    private Font boldFont(float size) {
        BaseFont baseFont = loadBaseFont(boldFontPath);
        return baseFont == null
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, size)
                : new Font(baseFont, size);
    }

    private BaseFont loadBaseFont(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return null;
        }

        try {
            Resource resource = resourceLoader.getResource("classpath:" + configuredPath);
            if (!resource.exists()) {
                return null;
            }

            return BaseFont.createFont(
                    resource.getFile().getAbsolutePath(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }
}
