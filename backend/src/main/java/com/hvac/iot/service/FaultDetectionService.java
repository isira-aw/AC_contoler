package com.hvac.iot.service;

import com.hvac.iot.dto.FaultLogDTO;
import com.hvac.iot.model.Device;
import com.hvac.iot.model.FaultLog;
import com.hvac.iot.model.Telemetry;
import com.hvac.iot.repository.DeviceRepository;
import com.hvac.iot.repository.FaultLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaultDetectionService {

    private final FaultLogRepository faultLogRepository;
    private final DeviceRepository deviceRepository;

    // Fault thresholds
    private static final float OVERCURRENT_THRESHOLD = 20.0f; // Amps
    private static final float VOLTAGE_MIN = 200.0f; // Volts
    private static final float VOLTAGE_MAX = 250.0f; // Volts
    private static final float OVERHEATING_THRESHOLD = 45.0f; // Celsius
    private static final float FILTER_CHOKE_THRESHOLD = 50.0f; // Pa
    private static final long OFFLINE_THRESHOLD_MINUTES = 2;

    @Transactional
    public void detectFaults(Device device, Telemetry telemetry) {
        // 1. Overcurrent Detection
        if (telemetry.getCurrent() != null && telemetry.getCurrent() > OVERCURRENT_THRESHOLD) {
            logFault(device, "Overcurrent", String.format("Current: %.2fA (threshold: %.2fA)",
                telemetry.getCurrent(), OVERCURRENT_THRESHOLD));
        }

        // 2. Phase Failure Detection (voltage out of range)
        if (telemetry.getVoltage() != null) {
            if (telemetry.getVoltage() < VOLTAGE_MIN || telemetry.getVoltage() > VOLTAGE_MAX) {
                logFault(device, "Phase Failure", String.format("Voltage: %.2fV (range: %.0f-%.0fV)",
                    telemetry.getVoltage(), VOLTAGE_MIN, VOLTAGE_MAX));
            }
        }

        // 3. Overheating Detection
        if (telemetry.getSupplyTemp() != null && telemetry.getSupplyTemp() > OVERHEATING_THRESHOLD) {
            logFault(device, "Overheating", String.format("Supply temp: %.2f°C (threshold: %.2f°C)",
                telemetry.getSupplyTemp(), OVERHEATING_THRESHOLD));
        }

        // 4. Filter Choke Detection
        if (telemetry.getFilterCondition() != null && telemetry.getFilterCondition() > FILTER_CHOKE_THRESHOLD) {
            logFault(device, "Filter Choke", String.format("Filter pressure: %.2f Pa (threshold: %.2f Pa)",
                telemetry.getFilterCondition(), FILTER_CHOKE_THRESHOLD));
        }

        // 5. Sensor Failure Detection (out-of-range or missing values)
        if (isSensorFailed(telemetry)) {
            logFault(device, "Sensor Failure", "One or more sensors reporting invalid values");
        }
    }

    private boolean isSensorFailed(Telemetry telemetry) {
        // Check for out-of-range values
        if (telemetry.getRoomTemp() != null && (telemetry.getRoomTemp() < -40 || telemetry.getRoomTemp() > 80)) {
            return true;
        }
        if (telemetry.getHumidity() != null && (telemetry.getHumidity() < 0 || telemetry.getHumidity() > 100)) {
            return true;
        }
        if (telemetry.getVoltage() != null && telemetry.getVoltage() < 0) {
            return true;
        }
        if (telemetry.getCurrent() != null && telemetry.getCurrent() < 0) {
            return true;
        }
        return false;
    }

    private void logFault(Device device, String faultType, String value) {
        // Check if there's already an unresolved fault of the same type
        List<FaultLog> existingFaults = faultLogRepository.findUnresolvedByDeviceIdAndType(device.getId(), faultType);
        if (!existingFaults.isEmpty()) {
            // Don't log duplicate faults
            return;
        }

        FaultLog fault = FaultLog.builder()
                .device(device)
                .faultType(faultType)
                .value(value)
                .timestamp(LocalDateTime.now())
                .resolved(false)
                .build();

        faultLogRepository.save(fault);
        log.warn("Fault detected - Device: {}, Type: {}, Value: {}", device.getId(), faultType, value);
    }

    // Scheduled task to check for offline devices - runs every minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkOfflineDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);

        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            if (device.getLicenseActive() && device.getLastHeartbeat() != null) {
                if (device.getLastHeartbeat().isBefore(threshold)) {
                    logFault(device, "Device Offline",
                        String.format("No heartbeat for over %d minutes", OFFLINE_THRESHOLD_MINUTES));
                }
            }
        }
    }

    public List<FaultLogDTO> getFaultsByDevice(String deviceId) {
        return faultLogRepository.findByDeviceIdOrderByTimestampDesc(deviceId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<FaultLogDTO> getUnresolvedFaults(String deviceId) {
        return faultLogRepository.findByDeviceIdAndResolvedFalseOrderByTimestampDesc(deviceId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FaultLogDTO resolveFault(UUID faultId) {
        FaultLog fault = faultLogRepository.findById(faultId)
                .orElseThrow(() -> new RuntimeException("Fault not found"));

        fault.setResolved(true);
        fault.setResolvedAt(LocalDateTime.now());
        fault = faultLogRepository.save(fault);

        log.info("Fault resolved - ID: {}, Type: {}", faultId, fault.getFaultType());
        return mapToDTO(fault);
    }

    private FaultLogDTO mapToDTO(FaultLog fault) {
        return FaultLogDTO.builder()
                .id(fault.getId())
                .deviceId(fault.getDevice().getId())
                .timestamp(fault.getTimestamp())
                .faultType(fault.getFaultType())
                .value(fault.getValue())
                .resolved(fault.getResolved())
                .resolvedAt(fault.getResolvedAt())
                .build();
    }
}
