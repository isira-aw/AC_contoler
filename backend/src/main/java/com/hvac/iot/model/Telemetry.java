package com.hvac.iot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "telemetry", indexes = {
    @Index(name = "idx_telemetry_device_timestamp", columnList = "device_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "supply_temp")
    private Float supplyTemp;

    @Column(name = "return_temp")
    private Float returnTemp;

    @Column(name = "room_temp")
    private Float roomTemp;

    @Column
    private Float humidity;

    @Column(name = "outdoor_temp")
    private Float outdoorTemp;

    @Column
    private Float voltage;

    @Column
    private Float current;

    @Column
    private Float power; // Calculated: voltage * current

    @Column
    private Float energy; // Accumulated kWh

    @Column(name = "compressor_status")
    private Boolean compressorStatus;

    @Column(name = "fan_speed")
    private String fanSpeed; // LOW, MED, HIGH

    @Column(name = "airflow_status")
    private Boolean airflowStatus;

    @Column(name = "filter_condition")
    private Float filterCondition;

    @Column(name = "refrigerant_pressure")
    private Float refrigerantPressure;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        // Calculate power from voltage and current
        if (voltage != null && current != null) {
            power = voltage * current / 1000; // Convert to kW
        }
    }
}
