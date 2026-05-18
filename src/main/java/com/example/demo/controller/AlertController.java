package com.example.demo.controller;

import java.util.List;

import org.springframework.http.MediaType;
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

import com.example.demo.dto.AssignRequest;
import com.example.demo.dto.StatusChangeRequest;
import com.example.demo.enums.StatusType;
import com.example.demo.model.Alert;
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
}
