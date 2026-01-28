package com.hvac.iot.service;

import com.hvac.iot.dto.PredictionResponse;
import com.hvac.iot.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final TelemetryRepository telemetryRepository;

    // Rated power for efficiency calculation (kW)
    private static final double RATED_POWER = 5.0;

    // Maximum weekly runtime hours
    private static final double MAX_WEEKLY_RUNTIME_HOURS = 24 * 7; // 168 hours

    public PredictionResponse getPredictions(String deviceId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // Get average power consumption over the last week
        Double avgPowerLastWeek = telemetryRepository.getAveragePowerSince(deviceId, oneWeekAgo);
        if (avgPowerLastWeek == null) {
            avgPowerLastWeek = 0.0;
        }

        // Get total energy consumption over the last week
        Double totalEnergyLastWeek = telemetryRepository.getTotalEnergySince(deviceId, oneWeekAgo);
        if (totalEnergyLastWeek == null) {
            totalEnergyLastWeek = 0.0;
        }

        // Get compressor runtime count (as a proxy for runtime hours)
        Long compressorRuntimeCount = telemetryRepository.getCompressorRuntimeCountSince(deviceId, oneWeekAgo);
        if (compressorRuntimeCount == null) {
            compressorRuntimeCount = 0L;
        }

        // Calculate runtime estimate (remaining capacity)
        // Assuming each telemetry record represents about 10 seconds of runtime
        double runtimeLastWeekHours = compressorRuntimeCount * 10.0 / 3600.0;
        double runtimeEstimate = Math.max(0, MAX_WEEKLY_RUNTIME_HOURS - runtimeLastWeekHours);

        // Calculate daily energy prediction
        double dailyEnergyPrediction = avgPowerLastWeek * 24;

        // Calculate monthly energy prediction
        double monthlyEnergyPrediction = dailyEnergyPrediction * 30;

        // Calculate efficiency score (0-100)
        double efficiencyScore = calculateEfficiencyScore(avgPowerLastWeek);

        return PredictionResponse.builder()
                .deviceId(deviceId)
                .runtimeEstimate(Math.round(runtimeEstimate * 100.0) / 100.0)
                .dailyEnergyPrediction(Math.round(dailyEnergyPrediction * 100.0) / 100.0)
                .monthlyEnergyPrediction(Math.round(monthlyEnergyPrediction * 100.0) / 100.0)
                .efficiencyScore(Math.round(efficiencyScore * 100.0) / 100.0)
                .build();
    }

    private double calculateEfficiencyScore(double avgPower) {
        if (avgPower <= 0) {
            return 100.0; // No consumption = 100% efficient (or device is off)
        }

        // Efficiency decreases as power consumption approaches or exceeds rated power
        double powerRatio = avgPower / RATED_POWER;

        // Calculate deviation factor (higher when power usage varies from optimal)
        double deviationFactor = 1.0 + (Math.abs(powerRatio - 0.7) * 0.5);

        // Calculate efficiency score
        double efficiencyScore = 100 - ((powerRatio * deviationFactor) * 30);

        // Clamp to 0-100 range
        return Math.max(0, Math.min(100, efficiencyScore));
    }
}
