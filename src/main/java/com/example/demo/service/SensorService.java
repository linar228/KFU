package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Sensor;
import com.example.demo.repository.SensorRepository;

@Service
public class SensorService {
    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public List<Sensor> getAll() {
        return sensorRepository.findAll();
    }

    public Sensor create(Sensor sensor) {
        return sensorRepository.save(sensor);
    }

    public Sensor update(Long id, Sensor request) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + id));
        sensor.setModel(request.getModel());
        sensor.setLocation(request.getLocation());
        sensor.setAssignedTo(request.getAssignedTo());
        return sensorRepository.save(sensor);
    }

    public void delete(Long id) {
        sensorRepository.deleteById(id);
    }
}
