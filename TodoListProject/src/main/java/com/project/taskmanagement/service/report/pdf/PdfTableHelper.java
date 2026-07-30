package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfTableHelper {

    PdfReportTheme theme;
    PdfReportHelper pdfReportHelper;

    public PdfPTable fullWidthTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        return table;
    }

    public PdfPCell headerCell(String value) {
        Font font = pdfReportHelper.boldFont(9);
        font.setColor(Color.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, font));
        cell.setBackgroundColor(new Color(30, 41, 59));
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setPadding(6f);
        return cell;
    }

    public PdfPCell textCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        Font font = pdfReportHelper.regularFont(8.5f);
        font.setColor(new Color(30, 41, 59));

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5.5f);
        cell.setBorderColor(new Color(226, 232, 240));

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
        }

        return cell;
    }

    public PdfPTable keyValueTable(Object... values) {
        PdfPTable table = fullWidthTable(2);
        for (int index = 0; index < values.length; index += 2) {
            Object key = values[index];
            Object value = index + 1 < values.length ? values[index + 1] : null;
            table.addCell(headerCell(key == null ? "" : String.valueOf(key)));
            table.addCell(textCell(value));
        }
        return table;
    }
}
