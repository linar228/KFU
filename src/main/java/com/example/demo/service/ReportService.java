package com.example.demo.service;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.example.demo.model.Alert;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateResolvedAlertReport(Alert alert) {
        try {
            Path directory = Path.of("reports", "alerts");
            Files.createDirectories(directory);
            Path reportPath = directory.resolve("alert-" + alert.getId() + "-resolved.pdf");

            try (OutputStream outputStream = Files.newOutputStream(reportPath)) {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, outputStream);
                document.open();

                Font titleFont = createFont(18, Font.BOLD);
                Font labelFont = createFont(12, Font.BOLD);
                Font valueFont = createFont(12, Font.NORMAL);

                Paragraph title = new Paragraph("Resolved alert report #" + alert.getId(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.2f, 2.8f});

                addRow(table, "Status", alert.getStatus().getValue(), labelFont, valueFont);
                addRow(table, "Event type", alert.getType() == null ? "" : alert.getType().getValue(), labelFont, valueFont);
                addRow(table, "Timestamp", alert.getTimestamp() == null ? "" : alert.getTimestamp().format(DATE_FORMAT), labelFont, valueFont);
                addRow(table, "Description", nullToEmpty(alert.getDescription()), labelFont, valueFont);
                addRow(table, "Sensor", alert.getSensor().getModel(), labelFont, valueFont);
                addRow(table, "Location", alert.getSensor().getLocation(), labelFont, valueFont);
                addRow(table, "Assigned to",
                        alert.getSensor().getAssignedTo() == null ? "Not assigned" : alert.getSensor().getAssignedTo().getUsername(),
                        labelFont, valueFont);
                addRow(table, "Photos", alert.getPhotoUrls().isEmpty() ? "None" : String.join("\n", alert.getPhotoUrls()), labelFont, valueFont);

                document.add(table);
                document.close();
            }

            return reportPath.toString();
        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException("Could not generate PDF report", exception);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new Color(235, 240, 245));
        labelCell.setPadding(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private Font createFont(int size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (DocumentException | IOException exception) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
