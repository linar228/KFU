package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Alert;
import com.example.demo.service.AlertService;

@Controller
public class AlertPageController {
    private final AlertService alertService;

    public AlertPageController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/incidents/{id}/photos")
    @PreAuthorize("hasAuthority('INCIDENT:PHOTO')")
    public String photoForm(@PathVariable Long id, Model model) {
        model.addAttribute("alert", alertService.getById(id));
        return "photo-upload";
    }

    @PostMapping("/incidents/{id}/photos")
    @PreAuthorize("hasAuthority('INCIDENT:PHOTO')")
    public String uploadPhoto(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            Model model
    ) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("alert", alertService.getById(id));
            model.addAttribute("error", "Выберите файл для загрузки");
            return "photo-upload";
        }

        Path directory = Path.of("uploads", "alerts", id.toString());
        Files.createDirectories(directory);
        String filename = StringUtils.cleanPath(String.valueOf(file.getOriginalFilename()));
        Path target = directory.resolve(System.currentTimeMillis() + "-" + filename);
        file.transferTo(target);

        Alert alert = alertService.addPhoto(id, target.toString());
        model.addAttribute("alert", alert);
        model.addAttribute("message", "Фото загружено");
        return "photo-upload";
    }
}
