package com.hvac.iot.controller;

import com.hvac.iot.dto.*;
import com.hvac.iot.security.UserPrincipal;
import com.hvac.iot.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class DeviceOwnerController {

    private final TeamService teamService;
    private final UserService userService;
    private final DeviceService deviceService;
    private final TelemetryService telemetryService;
    private final FaultDetectionService faultDetectionService;
    private final PdfExportService pdfExportService;

    // Team Management
    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<TeamDTO>> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            TeamDTO team = teamService.createTeam(request, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("Team created successfully", team));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getTeams(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TeamDTO> teams = teamService.getTeamsByOwner(principal.getUserIdAsUUID());
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<ApiResponse<TeamDTO>> getTeam(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            TeamDTO team = teamService.getTeamById(teamId, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success(team));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<ApiResponse<String>> deleteTeam(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            teamService.deleteTeam(teamId, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("Team deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // User Management
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<DeviceUserDTO>> createUser(
            @Valid @RequestBody RegisterDeviceUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DeviceUserDTO user = userService.createDeviceUser(request, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<DeviceUserDTO>>> getUsers(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DeviceUserDTO> users = userService.getDeviceUsersByOwner(principal.getUserIdAsUUID());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<DeviceUserDTO>> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateDeviceUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DeviceUserDTO user = userService.updateDeviceUser(userId, request, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            userService.deleteDeviceUser(userId, principal.getUserIdAsUUID());
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Device Management
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceDTO>>> getDevices(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DeviceDTO> devices = deviceService.getDevicesByOwner(principal.getUserIdAsUUID());
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceDTO>> getDevice(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            DeviceDTO device = deviceService.getDeviceById(deviceId);
            return ResponseEntity.ok(ApiResponse.success(device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/devices/{deviceId}/transfer")
    public ResponseEntity<ApiResponse<DeviceDTO>> transferDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody TransferDeviceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DeviceDTO device = deviceService.transferDevice(
                deviceId, principal.getUserIdAsUUID(), request.getNewOwnerId());
            return ResponseEntity.ok(ApiResponse.success("Device transferred successfully", device));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Telemetry
    @GetMapping("/devices/{deviceId}/telemetry")
    public ResponseEntity<ApiResponse<List<TelemetryDTO>>> getTelemetry(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
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
            if (!deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            TelemetryDTO telemetry = telemetryService.getLatestTelemetry(deviceId);
            return ResponseEntity.ok(ApiResponse.success(telemetry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Faults
    @GetMapping("/devices/{deviceId}/faults")
    public ResponseEntity<ApiResponse<List<FaultLogDTO>>> getFaults(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            List<FaultLogDTO> faults = faultDetectionService.getFaultsByDevice(deviceId);
            return ResponseEntity.ok(ApiResponse.success(faults));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/faults/{faultId}/resolve")
    public ResponseEntity<ApiResponse<FaultLogDTO>> resolveFault(
            @PathVariable UUID faultId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            FaultLogDTO fault = faultDetectionService.resolveFault(faultId);
            return ResponseEntity.ok(ApiResponse.success("Fault resolved", fault));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/devices/{deviceId}/faults/export-pdf")
    public ResponseEntity<byte[]> exportFaultsPdf(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (!deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId)) {
                return ResponseEntity.status(403).build();
            }

            byte[] pdfBytes = pdfExportService.exportFaultLogsToPdf(deviceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "fault_log_" + deviceId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Dashboard Stats
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DeviceDTO> devices = deviceService.getDevicesByOwner(principal.getUserIdAsUUID());
        List<TeamDTO> teams = teamService.getTeamsByOwner(principal.getUserIdAsUUID());
        List<DeviceUserDTO> users = userService.getDeviceUsersByOwner(principal.getUserIdAsUUID());

        long activeDevices = devices.stream().filter(d -> d.getLicenseActive() != null && d.getLicenseActive()).count();
        long totalFaults = devices.stream().mapToLong(d -> d.getUnresolvedFaults() != null ? d.getUnresolvedFaults() : 0).sum();

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalDevices(devices.size())
                .activeDevices(activeDevices)
                .totalTeams(teams.size())
                .totalDeviceUsers(users.size())
                .unresolvedFaults(totalFaults)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
