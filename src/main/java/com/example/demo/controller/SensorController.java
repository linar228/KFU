package com.example.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BulkImportResult;
import com.example.demo.model.Sensor;
import com.example.demo.service.BulkImportService;
import com.example.demo.service.FileExportService;
import com.example.demo.service.SensorService;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;
    private final BulkImportService bulkImportService;
    private final FileExportService fileExportService;

    public SensorController(
            SensorService sensorService,
            BulkImportService bulkImportService,
            FileExportService fileExportService
    ) {
        this.sensorService = sensorService;
        this.bulkImportService = bulkImportService;
        this.fileExportService = fileExportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SENSOR:READ')")
    public List<Sensor> getAll() {
        return sensorService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SENSOR:WRITE')")
    public Sensor create(@RequestBody Sensor sensor) {
        return sensorService.create(sensor);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SENSOR:WRITE')")
    public BulkImportResult importSensors(@RequestParam MultipartFile file) {
        return bulkImportService.importSensors(file);
    }

    @GetMapping("/export.csv")
    @PreAuthorize("hasAuthority('SENSOR:READ')")
    public ResponseEntity<byte[]> exportSensorsCsv() {
        return download("sensors.csv", "text/csv", fileExportService.exportSensorsCsv(sensorService.getAll()));
    }

    @GetMapping("/export.xlsx")
    @PreAuthorize("hasAuthority('SENSOR:READ')")
    public ResponseEntity<byte[]> exportSensorsXlsx() {
        return download(
                "sensors.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileExportService.exportSensorsXlsx(sensorService.getAll())
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SENSOR:WRITE')")
    public Sensor update(@PathVariable Long id, @RequestBody Sensor sensor) {
        return sensorService.update(id, sensor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SENSOR:DELETE')")
    public void delete(@PathVariable Long id) {
        sensorService.delete(id);
    }

    private ResponseEntity<byte[]> download(String filename, String contentType, byte[] content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}
