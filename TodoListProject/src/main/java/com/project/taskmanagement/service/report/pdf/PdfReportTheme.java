package com.project.taskmanagement.service.report.pdf;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PdfReportTheme {

    private final float pageMargin = 36F;

    private final float titleFontSize = 18F;

    private final float headingFontSize = 13F;

    private final float normalFontSize = 10F;

    private final float smallFontSize = 8F;

    private final float tableCellPadding = 5F;
}
