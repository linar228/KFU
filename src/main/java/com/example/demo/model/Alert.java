package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.demo.enums.EventType;
import com.example.demo.enums.StatusType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private LocalDateTime timestamp;

    private String description;

    @Enumerated(EnumType.STRING)
    private StatusType status = StatusType.NEW;

    @ElementCollection
    private List<String> photoUrls = new ArrayList<>();

    private String reportPath;

    @PrePersist
    void setDefaults() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusType.NEW;
        }
    }
}
