package com.example.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Sensor;
import com.example.demo.service.SensorService;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
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
}
