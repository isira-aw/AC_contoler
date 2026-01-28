package com.hvac.iot.controller;

import com.hvac.iot.dto.*;
import com.hvac.iot.security.UserPrincipal;
import com.hvac.iot.service.DeviceService;
import com.hvac.iot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final DeviceService deviceService;

    // Device Owner Management
    @PostMapping("/device-owners")
    public ResponseEntity<ApiResponse<DeviceOwnerDTO>> createDeviceOwner(
            @Valid @RequestBody RegisterDeviceOwnerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DeviceOwnerDTO owner = userService.createDeviceOwner(request, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("Device owner created successfully", owner));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/device-owners")
    public ResponseEntity<ApiResponse<PageResponse<DeviceOwnerDTO>>> getAllDeviceOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DeviceOwnerDTO> owners = userService.getAllDeviceOwnersPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(owners));
    }

    @GetMapping("/device-owners/{id}")
    public ResponseEntity<ApiResponse<DeviceOwnerDTO>> getDeviceOwner(@PathVariable String id) {
        try {
            DeviceOwnerDTO owner = userService.getDeviceOwnerById(java.util.UUID.fromString(id));
            return ResponseEntity.ok(ApiResponse.success(owner));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Device Management
    @PostMapping("/devices")
    public ResponseEntity<ApiResponse<DeviceDTO>> createDevice(
            @Valid @RequestBody RegisterDeviceRequest request) {
        try {
            DeviceDTO device = deviceService.createDevice(request);
            return ResponseEntity.ok(ApiResponse.success("Device created successfully", device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<PageResponse<DeviceDTO>>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DeviceDTO> devices = deviceService.getAllDevicesPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceDTO>> getDevice(@PathVariable String deviceId) {
        try {
            DeviceDTO device = deviceService.getDeviceById(deviceId);
            return ResponseEntity.ok(ApiResponse.success(device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceDTO>> updateDevice(
            @PathVariable String deviceId,
            @RequestBody UpdateDeviceRequest request) {
        try {
            DeviceDTO device = deviceService.updateDevice(deviceId, request);
            return ResponseEntity.ok(ApiResponse.success("Device updated successfully", device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<String>> deleteDevice(@PathVariable String deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
            return ResponseEntity.ok(ApiResponse.success("Device deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Dashboard Stats
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        List<DeviceDTO> devices = deviceService.getAllDevices();
        List<DeviceOwnerDTO> owners = userService.getAllDeviceOwners();

        long activeDevices = devices.stream().filter(d -> d.getLicenseActive() != null && d.getLicenseActive()).count();
        long licensedDevices = devices.stream().filter(d -> d.getLicenseActive() != null && d.getLicenseActive()).count();
        long totalFaults = devices.stream().mapToLong(d -> d.getUnresolvedFaults() != null ? d.getUnresolvedFaults() : 0).sum();

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalDevices(devices.size())
                .activeDevices(activeDevices)
                .totalDeviceOwners(owners.size())
                .licensedDevices(licensedDevices)
                .unlicensedDevices(devices.size() - licensedDevices)
                .totalFaults(totalFaults)
                .unresolvedFaults(totalFaults)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
