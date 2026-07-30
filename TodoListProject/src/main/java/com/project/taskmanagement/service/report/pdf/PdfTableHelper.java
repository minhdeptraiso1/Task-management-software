package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
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

    public PdfPTable fullWidthTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    public PdfPCell headerCell(String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, theme.getSmallFontSize());
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(theme.getTableCellPadding());
        return cell;
    }

    public PdfPCell textCell(Object value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, theme.getSmallFontSize());
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : String.valueOf(value), font));
        cell.setPadding(theme.getTableCellPadding());
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
