package com.hvac.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private String deviceId;
    private Double runtimeEstimate;          // Hours remaining
    private Double dailyEnergyPrediction;    // kWh
    private Double monthlyEnergyPrediction;  // kWh
    private Double efficiencyScore;          // 0-100
}
