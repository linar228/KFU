package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Alert;
import com.example.demo.model.StatusType;
import com.example.demo.service.AlertService;

@RestController
@RequestMapping("/api/incidents")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT:WRITE')")
    public Alert create(@RequestBody Alert alert) {
        return alertService.create(alert);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    public List<Alert> getAll(@RequestParam(required = false) String status) {
        StatusType statusType = status == null ? null : StatusType.fromValue(status);
        return alertService.getAll(statusType);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('INCIDENT:ASSIGN')")
    public Alert assign(@PathVariable Long id, @RequestBody AssignRequest request) {
        return alertService.assign(id, request.getUserId());
    }

    @PutMapping("/{id}/status_change")
    @PreAuthorize("hasAuthority('INCIDENT:STATUS')")
    public Alert changeStatus(@PathVariable Long id, @RequestBody StatusChangeRequest request) {
        return alertService.changeStatus(id, StatusType.fromValue(request.getStatus()));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('INCIDENT:PHOTO')")
    public Alert uploadPhoto(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String url
    ) throws IOException {
        if (url != null && !url.isBlank()) {
            return alertService.addPhoto(id, url);
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file or url is required");
        }

        Path directory = Path.of("uploads", "alerts", id.toString());
        Files.createDirectories(directory);
        String filename = StringUtils.cleanPath(String.valueOf(file.getOriginalFilename()));
        Path target = directory.resolve(System.currentTimeMillis() + "-" + filename);
        file.transferTo(target);
        return alertService.addPhoto(id, target.toString());
    }

    public static class AssignRequest {
        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    public static class StatusChangeRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
