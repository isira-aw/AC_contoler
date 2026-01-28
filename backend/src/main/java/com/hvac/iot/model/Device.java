package com.hvac.iot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(name = "id")
    private String id; // Assigned by SuperAdmin

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private DeviceOwner owner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "license_active", nullable = false)
    @Builder.Default
    private Boolean licenseActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    // Current state fields
    @Column(name = "power_status")
    @Builder.Default
    private String powerStatus = "OFF";

    @Column(name = "mode")
    @Builder.Default
    private String mode = "COOLING";

    @Column(name = "fan_speed")
    @Builder.Default
    private String fanSpeed = "LOW";

    @Column(name = "temperature_setpoint")
    @Builder.Default
    private Float temperatureSetpoint = 24.0f;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Telemetry> telemetryData = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FaultLog> faultLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
