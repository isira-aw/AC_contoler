package com.hvac.iot.service;

import com.hvac.iot.dto.*;
import com.hvac.iot.model.*;
import com.hvac.iot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceOwnerRepository deviceOwnerRepository;
    private final TeamRepository teamRepository;
    private final FaultLogRepository faultLogRepository;

    // SuperAdmin operations
    @Transactional
    public DeviceDTO createDevice(RegisterDeviceRequest request) {
        if (deviceRepository.existsById(request.getDeviceId())) {
            throw new RuntimeException("Device ID already exists");
        }

        Device device = Device.builder()
                .id(request.getDeviceId())
                .name(request.getName())
                .build();

        if (request.getOwnerId() != null) {
            DeviceOwner owner = deviceOwnerRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("DeviceOwner not found"));
            device.setOwner(owner);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            device.setTeam(team);
        }

        device = deviceRepository.save(device);
        log.info("Created device: {}", device.getId());

        return mapToDeviceDTO(device);
    }

    @Transactional
    public DeviceDTO updateDevice(String deviceId, UpdateDeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (request.getName() != null) {
            device.setName(request.getName());
        }

        if (request.getOwnerId() != null) {
            DeviceOwner owner = deviceOwnerRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("DeviceOwner not found"));
            device.setOwner(owner);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            device.setTeam(team);
        }

        if (request.getLicenseActive() != null) {
            device.setLicenseActive(request.getLicenseActive());
            log.info("Device {} license set to: {}", deviceId, request.getLicenseActive());
        }

        device = deviceRepository.save(device);
        return mapToDeviceDTO(device);
    }

    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<DeviceDTO> getAllDevicesPaged(Pageable pageable) {
        Page<Device> page = deviceRepository.findAll(pageable);
        List<DeviceDTO> content = page.getContent().stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    public PageResponse<DeviceDTO> getDevicesByOwnerPaged(UUID ownerId, Pageable pageable) {
        Page<Device> page = deviceRepository.findByOwnerId(ownerId, pageable);
        List<DeviceDTO> content = page.getContent().stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    public DeviceDTO getDeviceById(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return mapToDeviceDTO(device);
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        deviceRepository.delete(device);
        log.info("Deleted device: {}", deviceId);
    }

    // DeviceOwner operations
    public List<DeviceDTO> getDevicesByOwner(UUID ownerId) {
        return deviceRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceDTO transferDevice(String deviceId, UUID currentOwnerId, UUID newOwnerId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (device.getOwner() == null || !device.getOwner().getId().equals(currentOwnerId)) {
            throw new RuntimeException("Not authorized to transfer this device");
        }

        DeviceOwner newOwner = deviceOwnerRepository.findById(newOwnerId)
                .orElseThrow(() -> new RuntimeException("New owner not found"));

        device.setOwner(newOwner);
        device.setTeam(null); // Clear team assignment on transfer

        device = deviceRepository.save(device);
        log.info("Device {} transferred from {} to {}", deviceId, currentOwnerId, newOwnerId);

        return mapToDeviceDTO(device);
    }

    // DeviceUser operations
    public List<DeviceDTO> getDevicesForUser(UUID userId) {
        return deviceRepository.findDevicesForUser(userId).stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
    }

    public boolean canUserAccessDevice(UUID userId, String deviceId) {
        return deviceRepository.canUserAccessDevice(userId, deviceId);
    }

    public boolean canOwnerAccessDevice(UUID ownerId, String deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        return device != null && device.getOwner() != null && device.getOwner().getId().equals(ownerId);
    }

    public Device getDeviceEntity(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    @Transactional
    public void updateDeviceState(String deviceId, String powerStatus, String mode, String fanSpeed, Float temperatureSetpoint) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (powerStatus != null) device.setPowerStatus(powerStatus);
        if (mode != null) device.setMode(mode);
        if (fanSpeed != null) device.setFanSpeed(fanSpeed);
        if (temperatureSetpoint != null) device.setTemperatureSetpoint(temperatureSetpoint);

        deviceRepository.save(device);
    }

    private DeviceDTO mapToDeviceDTO(Device device) {
        long unresolvedFaults = faultLogRepository.countUnresolvedByDeviceId(device.getId());

        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .ownerId(device.getOwner() != null ? device.getOwner().getId() : null)
                .ownerName(device.getOwner() != null ? device.getOwner().getName() : null)
                .teamId(device.getTeam() != null ? device.getTeam().getId() : null)
                .teamName(device.getTeam() != null ? device.getTeam().getName() : null)
                .licenseActive(device.getLicenseActive())
                .createdAt(device.getCreatedAt())
                .lastHeartbeat(device.getLastHeartbeat())
                .powerStatus(device.getPowerStatus())
                .mode(device.getMode())
                .fanSpeed(device.getFanSpeed())
                .temperatureSetpoint(device.getTemperatureSetpoint())
                .unresolvedFaults(unresolvedFaults)
                .build();
    }
}
