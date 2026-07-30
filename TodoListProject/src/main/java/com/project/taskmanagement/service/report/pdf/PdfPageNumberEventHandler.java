package com.project.taskmanagement.service.report.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class PdfPageNumberEventHandler extends PdfPageEventHelper {

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle pageSize = document.getPageSize();
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Phrase footer = new Phrase("Page " + writer.getPageNumber(), font);
        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                footer,
                pageSize.getWidth() / 2,
                20,
                0
        );
    }
}
