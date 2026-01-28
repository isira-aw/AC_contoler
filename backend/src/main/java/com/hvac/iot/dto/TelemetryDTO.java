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
public class TelemetryDTO {
    private UUID id;
    private String deviceId;
    private LocalDateTime timestamp;
    private Float supplyTemp;
    private Float returnTemp;
    private Float roomTemp;
    private Float humidity;
    private Float outdoorTemp;
    private Float voltage;
    private Float current;
    private Float power;
    private Float energy;
    private Boolean compressorStatus;
    private String fanSpeed;
    private Boolean airflowStatus;
    private Float filterCondition;
    private Float refrigerantPressure;
}
