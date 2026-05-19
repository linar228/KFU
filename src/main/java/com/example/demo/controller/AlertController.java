package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.example.demo.dto.AssignRequest;
import com.example.demo.dto.StatusChangeRequest;
import com.example.demo.enums.StatusType;
import com.example.demo.model.Alert;
import com.example.demo.service.AlertService;
import com.example.demo.service.BulkImportService;
import com.example.demo.service.FileExportService;

@RestController
@RequestMapping("/api/incidents")
public class AlertController {
    private final AlertService alertService;
    private final BulkImportService bulkImportService;
    private final FileExportService fileExportService;

    public AlertController(
            AlertService alertService,
            BulkImportService bulkImportService,
            FileExportService fileExportService
    ) {
        this.alertService = alertService;
        this.bulkImportService = bulkImportService;
        this.fileExportService = fileExportService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT:WRITE')")
    public Alert create(@RequestBody Alert alert) {
        return alertService.create(alert);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('INCIDENT:WRITE')")
    public BulkImportResult importAlerts(@RequestParam MultipartFile file) {
        return bulkImportService.importAlerts(file);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    public List<Alert> getAll(@RequestParam(required = false) String status) {
        StatusType statusType = status == null ? null : StatusType.fromValue(status);
        return alertService.getAll(statusType);
    }

    @GetMapping("/export.csv")
    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    public ResponseEntity<byte[]> exportAlertsCsv(@RequestParam(required = false) String status) {
        StatusType statusType = status == null ? null : StatusType.fromValue(status);
        return download("incidents.csv", "text/csv", fileExportService.exportAlertsCsv(alertService.getAll(statusType)));
    }

    @GetMapping("/export.xlsx")
    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    public ResponseEntity<byte[]> exportAlertsXlsx(@RequestParam(required = false) String status) {
        StatusType statusType = status == null ? null : StatusType.fromValue(status);
        return download(
                "incidents.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileExportService.exportAlertsXlsx(alertService.getAll(statusType))
        );
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('INCIDENT:ASSIGN')")
    public Alert assign(@PathVariable Long id, @RequestBody AssignRequest request) {
        return alertService.assign(id, request.userId());
    }

    @PutMapping("/{id}/status_change")
    @PreAuthorize("hasAuthority('INCIDENT:STATUS')")
    public Alert changeStatus(@PathVariable Long id, @RequestBody StatusChangeRequest request) {
        return alertService.changeStatus(id, StatusType.fromValue(request.status()));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('INCIDENT:PHOTO')")
    public Alert uploadPhoto(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String url
    ) {
        if (url != null && !url.isBlank()) {
            return alertService.addPhoto(id, url);
        }
        return alertService.addPhotoFile(id, file);
    }

    private ResponseEntity<byte[]> download(String filename, String contentType, byte[] content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}
