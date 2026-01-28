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
public class FaultLogDTO {
    private UUID id;
    private String deviceId;
    private LocalDateTime timestamp;
    private String faultType;
    private String value;
    private Boolean resolved;
    private LocalDateTime resolvedAt;
}
