package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.properties.StorageProperties;

@Service
public class FileStorageService {
    private final StorageProperties storageProperties;

    public FileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public String saveAlertPhoto(Long alertId, MultipartFile file) {
        try {
            Path directory = Path.of(storageProperties.getUploadsPath(), "alerts", alertId.toString());
            Files.createDirectories(directory);

            String filename = StringUtils.cleanPath(String.valueOf(file.getOriginalFilename()));
            Path target = directory.resolve(System.currentTimeMillis() + "-" + filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save alert photo", exception);
        }
    }
}
