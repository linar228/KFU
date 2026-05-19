package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BulkImportResult;
import com.example.demo.model.User;
import com.example.demo.service.BulkImportService;
import com.example.demo.service.FileExportService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final BulkImportService bulkImportService;
    private final FileExportService fileExportService;

    public UserController(
            UserService userService,
            BulkImportService bulkImportService,
            FileExportService fileExportService
    ) {
        this.userService = userService;
        this.bulkImportService = bulkImportService;
        this.fileExportService = fileExportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER:READ')")
    public List<User> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER:WRITE')")
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('USER:WRITE')")
    public BulkImportResult importUsers(@RequestParam MultipartFile file) {
        return bulkImportService.importUsers(file);
    }

    @GetMapping("/export.csv")
    @PreAuthorize("hasAuthority('USER:READ')")
    public ResponseEntity<byte[]> exportUsersCsv() {
        return download("users.csv", "text/csv", fileExportService.exportUsersCsv(userService.getAll()));
    }

    @GetMapping("/export.xlsx")
    @PreAuthorize("hasAuthority('USER:READ')")
    public ResponseEntity<byte[]> exportUsersXlsx() {
        return download(
                "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileExportService.exportUsersXlsx(userService.getAll())
        );
    }

    private ResponseEntity<byte[]> download(String filename, String contentType, byte[] content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}
