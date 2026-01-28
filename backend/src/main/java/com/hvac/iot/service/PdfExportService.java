package com.hvac.iot.service;

import com.hvac.iot.model.Device;
import com.hvac.iot.model.FaultLog;
import com.hvac.iot.repository.DeviceRepository;
import com.hvac.iot.repository.FaultLogRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private final FaultLogRepository faultLogRepository;
    private final DeviceRepository deviceRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportFaultLogsToPdf(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        List<FaultLog> faults = faultLogRepository.findByDeviceIdOrderByTimestampDesc(deviceId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph title = new Paragraph("Fault Log Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

            // Add device info
            Paragraph deviceInfo = new Paragraph()
                    .add("Device ID: " + device.getId() + "\n")
                    .add("Device Name: " + device.getName() + "\n")
                    .add("Owner: " + (device.getOwner() != null ? device.getOwner().getName() : "N/A") + "\n")
                    .add("Generated: " + java.time.LocalDateTime.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(deviceInfo);

            // Create table
            float[] columnWidths = {2, 2, 3, 1.5f, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Add header row
            addHeaderCell(table, "Timestamp");
            addHeaderCell(table, "Fault Type");
            addHeaderCell(table, "Value");
            addHeaderCell(table, "Resolved");
            addHeaderCell(table, "Resolved At");

            // Add data rows
            for (FaultLog fault : faults) {
                addCell(table, fault.getTimestamp().format(DATE_FORMATTER));
                addCell(table, fault.getFaultType());
                addCell(table, fault.getValue() != null ? fault.getValue() : "N/A");
                addCell(table, fault.getResolved() ? "Yes" : "No");
                addCell(table, fault.getResolvedAt() != null ? fault.getResolvedAt().format(DATE_FORMATTER) : "-");
            }

            document.add(table);

            // Add summary
            long totalFaults = faults.size();
            long resolvedFaults = faults.stream().filter(FaultLog::getResolved).count();
            long unresolvedFaults = totalFaults - resolvedFaults;

            Paragraph summary = new Paragraph()
                    .add("\n\nSummary\n")
                    .setBold()
                    .setFontSize(12);
            document.add(summary);

            Paragraph summaryDetails = new Paragraph()
                    .add("Total Faults: " + totalFaults + "\n")
                    .add("Resolved: " + resolvedFaults + "\n")
                    .add("Unresolved: " + unresolvedFaults)
                    .setFontSize(10);
            document.add(summaryDetails);

            document.close();

            log.info("Generated PDF fault report for device: {}", deviceId);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF for device: {}", deviceId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9);
        table.addHeaderCell(cell);
    }

    private void addCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8);
        table.addCell(cell);
    }
}
