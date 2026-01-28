package com.hvac.iot.service;

import com.hvac.iot.dto.TelemetryDTO;
import com.hvac.iot.model.Device;
import com.hvac.iot.model.Telemetry;
import com.hvac.iot.repository.DeviceRepository;
import com.hvac.iot.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final DeviceRepository deviceRepository;
    private final FaultDetectionService faultDetectionService;

    @Transactional
    public TelemetryDTO saveTelemetry(String deviceId, TelemetryDTO dto) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        // Calculate power from voltage and current
        Float power = null;
        if (dto.getVoltage() != null && dto.getCurrent() != null) {
            power = dto.getVoltage() * dto.getCurrent() / 1000; // Convert to kW
        }

        Telemetry telemetry = Telemetry.builder()
                .device(device)
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .supplyTemp(dto.getSupplyTemp())
                .returnTemp(dto.getReturnTemp())
                .roomTemp(dto.getRoomTemp())
                .humidity(dto.getHumidity())
                .outdoorTemp(dto.getOutdoorTemp())
                .voltage(dto.getVoltage())
                .current(dto.getCurrent())
                .power(power)
                .energy(dto.getEnergy())
                .compressorStatus(dto.getCompressorStatus())
                .fanSpeed(dto.getFanSpeed())
                .airflowStatus(dto.getAirflowStatus())
                .filterCondition(dto.getFilterCondition())
                .refrigerantPressure(dto.getRefrigerantPressure())
                .build();

        telemetry = telemetryRepository.save(telemetry);

        // Update device heartbeat
        device.setLastHeartbeat(LocalDateTime.now());
        deviceRepository.save(device);

        // Run fault detection
        faultDetectionService.detectFaults(device, telemetry);

        return mapToDTO(telemetry);
    }

    public List<TelemetryDTO> getTelemetryByDevice(String deviceId, LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            from = LocalDateTime.now().minusDays(1);
        }
        if (to == null) {
            to = LocalDateTime.now();
        }

        return telemetryRepository.findByDeviceIdAndTimestampBetween(deviceId, from, to)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TelemetryDTO getLatestTelemetry(String deviceId) {
        return telemetryRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId)
                .map(this::mapToDTO)
                .orElse(null);
    }

    // Scheduled job to delete telemetry older than 7 days - runs at 2 AM every day
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldTelemetry() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        int deleted = telemetryRepository.deleteOlderThan(cutoff);
        log.info("Deleted {} telemetry records older than {}", deleted, cutoff);
    }

    private TelemetryDTO mapToDTO(Telemetry telemetry) {
        return TelemetryDTO.builder()
                .id(telemetry.getId())
                .deviceId(telemetry.getDevice().getId())
                .timestamp(telemetry.getTimestamp())
                .supplyTemp(telemetry.getSupplyTemp())
                .returnTemp(telemetry.getReturnTemp())
                .roomTemp(telemetry.getRoomTemp())
                .humidity(telemetry.getHumidity())
                .outdoorTemp(telemetry.getOutdoorTemp())
                .voltage(telemetry.getVoltage())
                .current(telemetry.getCurrent())
                .power(telemetry.getPower())
                .energy(telemetry.getEnergy())
                .compressorStatus(telemetry.getCompressorStatus())
                .fanSpeed(telemetry.getFanSpeed())
                .airflowStatus(telemetry.getAirflowStatus())
                .filterCondition(telemetry.getFilterCondition())
                .refrigerantPressure(telemetry.getRefrigerantPressure())
                .build();
    }
}
