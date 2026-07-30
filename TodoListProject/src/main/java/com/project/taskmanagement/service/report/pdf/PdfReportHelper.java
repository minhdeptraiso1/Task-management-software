package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
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
import java.io.File;
import java.io.InputStream;
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
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    final PdfReportTheme theme;
    final ResourceLoader resourceLoader;

    @Value("${app.report.pdf.font-path:fonts/NotoSans-Regular.ttf}")
    String fontPath;

    @Value("${app.report.pdf.bold-font-path:fonts/NotoSans-Bold.ttf}")
    String boldFontPath;

    BaseFont cachedRegularBaseFont;
    BaseFont cachedBoldBaseFont;

    public Document createDocument() {
        return new Document(
                PageSize.A4,
                theme.getPageMargin(),
                theme.getPageMargin(),
                theme.getPageMargin() + 10,
                theme.getPageMargin() + 10
        );
    }

    public Paragraph title(String text) {
        Font font = boldFont(15);
        font.setColor(Color.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(text.toUpperCase(), font));
        cell.setBackgroundColor(new Color(30, 41, 59)); // Dark Navy #1E293B
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(9f);
        cell.setPaddingBottom(9f);
        cell.setBorder(0);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.addCell(cell);

        Paragraph p = new Paragraph();
        p.add(table);
        p.setSpacingAfter(10);
        return p;
    }

    public Paragraph sectionTitle(String text) {
        Font font = boldFont(11.5f);
        font.setColor(new Color(30, 41, 59));

        PdfPCell cell = new PdfPCell(new Phrase("  " + text, font));
        cell.setBackgroundColor(new Color(248, 250, 252));
        cell.setBorderColorLeft(new Color(59, 130, 246));
        cell.setBorderWidthLeft(3.5f);
        cell.setBorderColorTop(new Color(226, 232, 240));
        cell.setBorderColorRight(new Color(226, 232, 240));
        cell.setBorderColorBottom(new Color(226, 232, 240));
        cell.setBorderWidthTop(1f);
        cell.setBorderWidthRight(1f);
        cell.setBorderWidthBottom(1f);
        cell.setPadding(5f);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.addCell(cell);

        Paragraph paragraph = new Paragraph();
        paragraph.add(table);
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(6);
        return paragraph;
    }

    public Paragraph normalText(String text) {
        Font font = regularFont(theme.getNormalFontSize());
        Paragraph paragraph = new Paragraph(text == null ? "" : text, font);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    public Paragraph generatedAt() {
        Font font = regularFont(8.5f);
        font.setColor(new Color(100, 116, 139));
        Paragraph paragraph = new Paragraph(
                "Thời gian xuất báo cáo: " + LocalDateTime.now(BUSINESS_ZONE).format(DATE_TIME_FORMATTER),
                font
        );
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        paragraph.setSpacingAfter(10);
        return paragraph;
    }

    public PdfPTable keyValueTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        try {
            table.setWidths(new float[]{35f, 65f});
        } catch (Exception ignored) {}
        return table;
    }

    public PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        return table;
    }

    public void addHeaderCell(PdfPTable table, String value) {
        Font font = boldFont(9);
        font.setColor(Color.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, font));
        cell.setBackgroundColor(new Color(30, 41, 59));
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setPadding(6f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    public void addCell(PdfPTable table, Object value) {
        addCell(table, value, false);
    }

    public void addCell(PdfPTable table, Object value, boolean isOddRow) {
        String text = format(value);
        Font font = regularFont(8.5f);
        font.setColor(new Color(30, 41, 59));

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5.5f);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String upperText = text.toUpperCase();
        if (upperText.equals("DONE") || upperText.equals("HOÀN THÀNH") || upperText.equals("RESOLVED") || upperText.equals("CLOSED")) {
            cell.setBackgroundColor(new Color(220, 252, 231));
            font.setColor(new Color(22, 101, 52));
            font.setStyle(Font.BOLD);
        } else if (upperText.equals("IN_PROGRESS") || upperText.equals("ĐANG LÀM") || upperText.equals("IN_REVIEW") || upperText.equals("ACTIVE")) {
            cell.setBackgroundColor(new Color(254, 243, 199));
            font.setColor(new Color(146, 64, 14));
            font.setStyle(Font.BOLD);
        } else if (upperText.equals("BLOCKED") || upperText.equals("ĐANG CHỜ") || upperText.equals("NGHẼN") || upperText.equals("CANCELLED") || upperText.equals("CRITICAL")) {
            cell.setBackgroundColor(new Color(254, 226, 226));
            font.setColor(new Color(153, 27, 27));
            font.setStyle(Font.BOLD);
        } else if (isOddRow) {
            cell.setBackgroundColor(new Color(248, 250, 252));
        }

        table.addCell(cell);
    }

    public void addKeyValueRow(PdfPTable table, String key, Object value) {
        addHeaderCell(table, key);
        addCell(table, value, false);
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

    public Font regularFont(float size) {
        BaseFont baseFont = getOrLoadBaseFont(false);
        return baseFont == null
                ? new Font(Font.HELVETICA, size)
                : new Font(baseFont, size);
    }

    public Font boldFont(float size) {
        BaseFont baseFont = getOrLoadBaseFont(true);
        return baseFont == null
                ? new Font(Font.HELVETICA, size, Font.BOLD)
                : new Font(baseFont, size);
    }

    public synchronized BaseFont getOrLoadBaseFont(boolean bold) {
        if (bold && cachedBoldBaseFont != null) {
            return cachedBoldBaseFont;
        }
        if (!bold && cachedRegularBaseFont != null) {
            return cachedRegularBaseFont;
        }

        BaseFont bf = loadBaseFont(bold);
        if (bold) {
            cachedBoldBaseFont = bf;
        } else {
            cachedRegularBaseFont = bf;
        }
        return bf;
    }

    private BaseFont loadBaseFont(boolean bold) {
        String configuredPath = bold ? boldFontPath : fontPath;
        if (configuredPath != null && !configuredPath.isBlank()) {
            try {
                Resource resource = resourceLoader.getResource("classpath:" + configuredPath);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        byte[] bytes = is.readAllBytes();
                        return BaseFont.createFont(configuredPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
                    }
                }
            } catch (Exception ignored) {}
        }

        String[] systemFontPaths = bold
                ? new String[]{
                    "C:\\Windows\\Fonts\\arialbd.ttf",
                    "C:\\Windows\\Fonts\\segoeuib.ttf",
                    "C:\\Windows\\Fonts\\tahomabd.ttf",
                    "C:\\Windows\\Fonts\\timesbd.ttf"
                }
                : new String[]{
                    "C:\\Windows\\Fonts\\arial.ttf",
                    "C:\\Windows\\Fonts\\segoeui.ttf",
                    "C:\\Windows\\Fonts\\tahoma.ttf",
                    "C:\\Windows\\Fonts\\times.ttf"
                };

        for (String sysPath : systemFontPaths) {
            try {
                File file = new File(sysPath);
                if (file.exists()) {
                    return BaseFont.createFont(sysPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}
