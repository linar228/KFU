package com.example.demo.dto;

import java.util.List;

public record BulkImportResult(int created, List<String> errors) {
}
