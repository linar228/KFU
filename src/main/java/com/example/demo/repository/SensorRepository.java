package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
