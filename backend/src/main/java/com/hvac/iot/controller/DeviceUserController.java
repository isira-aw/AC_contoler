package com.hvac.iot.controller;

import com.hvac.iot.dto.*;
import com.hvac.iot.security.UserPrincipal;
import com.hvac.iot.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class DeviceUserController {

    private final DeviceService deviceService;
    private final TelemetryService telemetryService;
    private final FaultDetectionService faultDetectionService;

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceDTO>>> getDevices(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DeviceDTO> devices = deviceService.getDevicesForUser(principal.getUserIdAsUUID());
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceDTO>> getDevice(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            DeviceDTO device = deviceService.getDeviceById(deviceId);
            return ResponseEntity.ok(ApiResponse.success(device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/devices/{deviceId}/telemetry")
    public ResponseEntity<ApiResponse<List<TelemetryDTO>>> getTelemetry(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            List<TelemetryDTO> telemetry = telemetryService.getTelemetryByDevice(deviceId, from, to);
            return ResponseEntity.ok(ApiResponse.success(telemetry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/devices/{deviceId}/telemetry/latest")
    public ResponseEntity<ApiResponse<TelemetryDTO>> getLatestTelemetry(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            TelemetryDTO telemetry = telemetryService.getLatestTelemetry(deviceId);
            return ResponseEntity.ok(ApiResponse.success(telemetry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/devices/{deviceId}/faults")
    public ResponseEntity<ApiResponse<List<FaultLogDTO>>> getFaults(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            List<FaultLogDTO> faults = faultDetectionService.getFaultsByDevice(deviceId);
            return ResponseEntity.ok(ApiResponse.success(faults));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DeviceDTO> devices = deviceService.getDevicesForUser(principal.getUserIdAsUUID());

        long activeDevices = devices.stream().filter(d -> d.getLicenseActive() != null && d.getLicenseActive()).count();
        long totalFaults = devices.stream().mapToLong(d -> d.getUnresolvedFaults() != null ? d.getUnresolvedFaults() : 0).sum();

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalDevices(devices.size())
                .activeDevices(activeDevices)
                .unresolvedFaults(totalFaults)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
