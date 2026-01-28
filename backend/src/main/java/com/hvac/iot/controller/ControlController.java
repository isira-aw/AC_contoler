package com.hvac.iot.controller;

import com.hvac.iot.dto.ApiResponse;
import com.hvac.iot.dto.ControlRequest;
import com.hvac.iot.model.Device;
import com.hvac.iot.security.UserPrincipal;
import com.hvac.iot.service.DeviceService;
import com.hvac.iot.service.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
@RequiredArgsConstructor
@Slf4j
public class ControlController {

    private final DeviceService deviceService;
    private final MqttService mqttService;

    private boolean checkAccess(UserPrincipal principal, String deviceId) {
        String role = principal.getRole();
        if ("DEVICE_OWNER".equals(role)) {
            return deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId);
        } else if ("DEVICE_USER".equals(role)) {
            return deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId);
        }
        return false;
    }

    private ResponseEntity<ApiResponse<String>> checkLicenseAndAccess(UserPrincipal principal, String deviceId) {
        if (!checkAccess(principal, deviceId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        Device device = deviceService.getDeviceEntity(deviceId);
        if (!device.getLicenseActive()) {
            return ResponseEntity.status(403).body(ApiResponse.error("Device license is inactive"));
        }

        return null; // No error
    }

    @PostMapping("/{deviceId}/on-off")
    public ResponseEntity<ApiResponse<String>> setOnOff(
            @PathVariable String deviceId,
            @RequestBody ControlRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ResponseEntity<ApiResponse<String>> error = checkLicenseAndAccess(principal, deviceId);
            if (error != null) return error;

            String status = request.getStatus();
            if (!"ON".equalsIgnoreCase(status) && !"OFF".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status. Use ON or OFF"));
            }

            mqttService.publishControlCommand(deviceId, "power", status.toUpperCase());
            deviceService.updateDeviceState(deviceId, status.toUpperCase(), null, null, null);

            log.info("Device {} power set to {} by user {}", deviceId, status, principal.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Device power set to " + status.toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/mode")
    public ResponseEntity<ApiResponse<String>> setMode(
            @PathVariable String deviceId,
            @RequestBody ControlRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ResponseEntity<ApiResponse<String>> error = checkLicenseAndAccess(principal, deviceId);
            if (error != null) return error;

            String mode = request.getMode();
            if (!"COOLING".equalsIgnoreCase(mode) && !"HEATING".equalsIgnoreCase(mode)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid mode. Use COOLING or HEATING"));
            }

            mqttService.publishControlCommand(deviceId, "mode", mode.toUpperCase());
            deviceService.updateDeviceState(deviceId, null, mode.toUpperCase(), null, null);

            log.info("Device {} mode set to {} by user {}", deviceId, mode, principal.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Device mode set to " + mode.toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/fan-speed")
    public ResponseEntity<ApiResponse<String>> setFanSpeed(
            @PathVariable String deviceId,
            @RequestBody ControlRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ResponseEntity<ApiResponse<String>> error = checkLicenseAndAccess(principal, deviceId);
            if (error != null) return error;

            String speed = request.getSpeed();
            if (!"LOW".equalsIgnoreCase(speed) && !"MED".equalsIgnoreCase(speed) && !"HIGH".equalsIgnoreCase(speed)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid speed. Use LOW, MED, or HIGH"));
            }

            mqttService.publishControlCommand(deviceId, "fan_speed", speed.toUpperCase());
            deviceService.updateDeviceState(deviceId, null, null, speed.toUpperCase(), null);

            log.info("Device {} fan speed set to {} by user {}", deviceId, speed, principal.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Device fan speed set to " + speed.toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/temperature")
    public ResponseEntity<ApiResponse<String>> setTemperature(
            @PathVariable String deviceId,
            @RequestBody ControlRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            ResponseEntity<ApiResponse<String>> error = checkLicenseAndAccess(principal, deviceId);
            if (error != null) return error;

            Float setpoint = request.getSetpoint();
            if (setpoint == null || setpoint < 16 || setpoint > 30) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid temperature. Range: 16-30°C"));
            }

            mqttService.publishControlCommand(deviceId, "temperature", String.valueOf(setpoint));
            deviceService.updateDeviceState(deviceId, null, null, null, setpoint);

            log.info("Device {} temperature set to {}°C by user {}", deviceId, setpoint, principal.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Device temperature set to " + setpoint + "°C"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
