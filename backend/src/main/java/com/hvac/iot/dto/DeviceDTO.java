package com.hvac.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    private String id;
    private String name;
    private UUID ownerId;
    private String ownerName;
    private UUID teamId;
    private String teamName;
    private Boolean licenseActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastHeartbeat;
    private String powerStatus;
    private String mode;
    private String fanSpeed;
    private Float temperatureSetpoint;
    private Long unresolvedFaults;
}
