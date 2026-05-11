package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Alert;
import com.example.demo.model.Sensor;
import com.example.demo.model.StatusType;
import com.example.demo.model.User;
import com.example.demo.repository.AlertRepository;
import com.example.demo.repository.SensorRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final SensorRepository sensorRepository;
    private final UserRepository userRepository;
    private final ReportService reportService;

    public AlertService(
            AlertRepository alertRepository,
            SensorRepository sensorRepository,
            UserRepository userRepository,
            ReportService reportService
    ) {
        this.alertRepository = alertRepository;
        this.sensorRepository = sensorRepository;
        this.userRepository = userRepository;
        this.reportService = reportService;
    }

    public Alert create(Alert alert) {
        Long sensorId = alert.getSensor() == null ? null : alert.getSensor().getId();
        if (sensorId == null) {
            throw new IllegalArgumentException("Sensor id is required");
        }

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + sensorId));
        alert.setSensor(sensor);
        return alertRepository.save(alert);
    }

    public List<Alert> getAll(StatusType status) {
        if (status == null) {
            return alertRepository.findAll();
        }
        return alertRepository.findByStatus(status);
    }

    @Transactional
    public Alert assign(Long alertId, Long userId) {
        Alert alert = getById(alertId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        alert.getSensor().setAssignedTo(user);
        alert.setStatus(StatusType.IN_PROGRESS);
        return alert;
    }

    @Transactional
    public Alert changeStatus(Long alertId, StatusType status) {
        Alert alert = getById(alertId);
        alert.setStatus(status);
        if (status == StatusType.RESOLVED) {
            alert.setReportPath(reportService.generateResolvedAlertReport(alert));
        }
        return alert;
    }

    @Transactional
    public Alert addPhoto(Long alertId, String photoUrl) {
        Alert alert = getById(alertId);
        alert.getPhotoUrls().add(photoUrl);
        return alert;
    }

    public Alert getById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
    }
}
