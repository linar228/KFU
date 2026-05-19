package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.demo.model.Alert;
import com.example.demo.model.Sensor;
import com.example.demo.model.User;

@Service
public class FileExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportSensorsCsv(List<Sensor> sensors) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "model", "location", "assignedToUsername"));
        for (Sensor sensor : sensors) {
            rows.add(List.of(
                    value(sensor.getId()),
                    value(sensor.getModel()),
                    value(sensor.getLocation()),
                    sensor.getAssignedTo() == null ? "" : value(sensor.getAssignedTo().getUsername())
            ));
        }
        return toCsv(rows);
    }

    public byte[] exportSensorsXlsx(List<Sensor> sensors) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "model", "location", "assignedToUsername"));
        for (Sensor sensor : sensors) {
            rows.add(List.of(
                    value(sensor.getId()),
                    value(sensor.getModel()),
                    value(sensor.getLocation()),
                    sensor.getAssignedTo() == null ? "" : value(sensor.getAssignedTo().getUsername())
            ));
        }
        return toXlsx("sensors", rows);
    }

    public byte[] exportUsersCsv(List<User> users) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "username", "role", "enabled"));
        for (User user : users) {
            rows.add(List.of(
                    value(user.getId()),
                    value(user.getUsername()),
                    user.getRole() == null ? "" : value(user.getRole().getName()),
                    value(user.isEnabled())
            ));
        }
        return toCsv(rows);
    }

    public byte[] exportUsersXlsx(List<User> users) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "username", "role", "enabled"));
        for (User user : users) {
            rows.add(List.of(
                    value(user.getId()),
                    value(user.getUsername()),
                    user.getRole() == null ? "" : value(user.getRole().getName()),
                    value(user.isEnabled())
            ));
        }
        return toXlsx("users", rows);
    }

    public byte[] exportAlertsCsv(List<Alert> alerts) {
        return toCsv(alertRows(alerts));
    }

    public byte[] exportAlertsXlsx(List<Alert> alerts) {
        return toXlsx("incidents", alertRows(alerts));
    }

    private List<List<String>> alertRows(List<Alert> alerts) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "sensorId", "type", "status", "timestamp", "description", "reportPath", "photoUrls"));
        for (Alert alert : alerts) {
            rows.add(List.of(
                    value(alert.getId()),
                    alert.getSensor() == null ? "" : value(alert.getSensor().getId()),
                    alert.getType() == null ? "" : alert.getType().getValue(),
                    alert.getStatus() == null ? "" : alert.getStatus().getValue(),
                    alert.getTimestamp() == null ? "" : alert.getTimestamp().format(DATE_FORMAT),
                    value(alert.getDescription()),
                    value(alert.getReportPath()),
                    String.join(";", alert.getPhotoUrls())
            ));
        }
        return rows;
    }

    private byte[] toCsv(List<List<String>> rows) {
        StringBuilder csv = new StringBuilder();
        for (List<String> row : rows) {
            csv.append(row.stream().map(this::escapeCsv).reduce((left, right) -> left + "," + right).orElse(""));
            csv.append("\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] toXlsx(String sheetName, List<List<String>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                List<String> values = rows.get(rowIndex);
                for (int cellIndex = 0; cellIndex < values.size(); cellIndex++) {
                    row.createCell(cellIndex).setCellValue(values.get(cellIndex));
                }
            }
            for (int index = 0; index < rows.get(0).size(); index++) {
                sheet.autoSizeColumn(index);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not export XLSX file", exception);
        }
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
