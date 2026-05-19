package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BulkImportResult;
import com.example.demo.enums.EventType;
import com.example.demo.enums.StatusType;
import com.example.demo.model.Alert;
import com.example.demo.model.Role;
import com.example.demo.model.Sensor;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.SensorRepository;
import com.example.demo.repository.UserRepository;

@Service
public class BulkImportService {
    private final SensorService sensorService;
    private final UserService userService;
    private final AlertService alertService;
    private final SensorRepository sensorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public BulkImportService(
            SensorService sensorService,
            UserService userService,
            AlertService alertService,
            SensorRepository sensorRepository,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.sensorService = sensorService;
        this.userService = userService;
        this.alertService = alertService;
        this.sensorRepository = sensorRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public BulkImportResult importSensors(MultipartFile file) {
        return importRows(file, this::createSensor);
    }

    public BulkImportResult importUsers(MultipartFile file) {
        return importRows(file, this::createUser);
    }

    public BulkImportResult importAlerts(MultipartFile file) {
        return importRows(file, this::createAlert);
    }

    private BulkImportResult importRows(MultipartFile file, RowImporter rowImporter) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Import file is required");
        }

        List<Map<String, String>> rows = readRows(file);
        List<String> errors = new ArrayList<>();
        int created = 0;

        for (int index = 0; index < rows.size(); index++) {
            try {
                rowImporter.importRow(rows.get(index));
                created++;
            } catch (RuntimeException exception) {
                errors.add("Row " + (index + 2) + ": " + exception.getMessage());
            }
        }

        return new BulkImportResult(created, errors);
    }

    private void createSensor(Map<String, String> row) {
        Sensor sensor = new Sensor();
        sensor.setModel(required(row, "model"));
        sensor.setLocation(required(row, "location"));

        String assignedTo = value(row, "assignedToUsername", "assignedTo", "username");
        if (!assignedTo.isBlank()) {
            User user = userRepository.findByUsername(assignedTo)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found: " + assignedTo));
            sensor.setAssignedTo(user);
        }

        sensorService.create(sensor);
    }

    private void createUser(Map<String, String> row) {
        User user = new User();
        user.setUsername(required(row, "username"));
        user.setPassword(required(row, "password"));
        user.setEnabled(parseBoolean(value(row, "enabled"), true));

        String roleName = value(row, "role", "roleName");
        if (!roleName.isBlank()) {
            Role role = roleRepository.findByName(roleName.toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            user.setRole(role);
        }

        userService.create(user);
    }

    private void createAlert(Map<String, String> row) {
        Long sensorId = Long.valueOf(required(row, "sensorId", "sensor_id"));
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + sensorId));

        Alert alert = new Alert();
        alert.setSensor(sensor);
        alert.setType(EventType.fromValue(required(row, "type", "eventType")));
        alert.setDescription(value(row, "description"));

        Alert saved = alertService.create(alert);
        String status = value(row, "status");
        if (!status.isBlank() && StatusType.fromValue(status) != StatusType.NEW) {
            alertService.changeStatus(saved.getId(), StatusType.fromValue(status));
        }
    }

    private List<Map<String, String>> readRows(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return readWorkbookRows(file);
            }
            if (filename.endsWith(".csv")) {
                return readCsvRows(file);
            }
            throw new IllegalArgumentException("Only CSV, XLS and XLSX files are supported");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read import file", exception);
        }
    }

    private List<Map<String, String>> readCsvRows(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> headers = parseCsvLine(reader.readLine());
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(toRow(headers, parseCsvLine(line)));
                }
            }
            return rows;
        }
    }

    private List<Map<String, String>> readWorkbookRows(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            List<Map<String, String>> rows = new ArrayList<>();

            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    values.add(formatter.formatCellValue(row.getCell(cellIndex)));
                }

                if (row.getRowNum() == 0) {
                    headers = values;
                } else if (!isEmpty(values)) {
                    rows.add(toRow(headers, values));
                }
            }
            return rows;
        }
    }

    private Map<String, String> toRow(List<String> headers, List<String> values) {
        Map<String, String> row = new HashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            String key = normalizeHeader(headers.get(index));
            String value = index < values.size() ? values.get(index).trim() : "";
            row.put(key, value);
        }
        return row;
    }

    private List<String> parseCsvLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Import file must contain a header row");
        }

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                quoted = !quoted;
            } else if (character == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private String required(Map<String, String> row, String... names) {
        String value = value(row, names);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Required column is missing: " + String.join("/", names));
        }
        return value;
    }

    private String value(Map<String, String> row, String... names) {
        for (String name : names) {
            String value = row.get(normalizeHeader(name));
            if (value != null) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private String normalizeHeader(String header) {
        return header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
    }

    private boolean isEmpty(List<String> values) {
        return values.stream().allMatch(String::isBlank);
    }

    @FunctionalInterface
    private interface RowImporter {
        void importRow(Map<String, String> row);
    }
}
