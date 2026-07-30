package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

public class PdfPageNumberEventHandler extends PdfPageEventHelper {

    private final PdfReportHelper pdfReportHelper;

    public PdfPageNumberEventHandler(PdfReportHelper pdfReportHelper) {
        this.pdfReportHelper = pdfReportHelper;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle pageSize = document.getPageSize();
        Font font = pdfReportHelper != null ? pdfReportHelper.regularFont(8f) : new Font(Font.HELVETICA, 8f);
        font.setColor(new Color(148, 163, 184));

        Phrase footer = new Phrase("Trang " + writer.getPageNumber(), font);
        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                footer,
                pageSize.getWidth() / 2,
                18,
                0
        );
    }
}
