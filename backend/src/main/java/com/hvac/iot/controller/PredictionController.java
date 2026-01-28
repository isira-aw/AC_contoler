package com.hvac.iot.controller;

import com.hvac.iot.dto.ApiResponse;
import com.hvac.iot.dto.PredictionResponse;
import com.hvac.iot.security.UserPrincipal;
import com.hvac.iot.service.DeviceService;
import com.hvac.iot.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final DeviceService deviceService;

    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<PredictionResponse>> getPredictions(
            @PathVariable String deviceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            String role = principal.getRole();
            boolean hasAccess = false;

            if ("DEVICE_OWNER".equals(role)) {
                hasAccess = deviceService.canOwnerAccessDevice(principal.getUserIdAsUUID(), deviceId);
            } else if ("DEVICE_USER".equals(role)) {
                hasAccess = deviceService.canUserAccessDevice(principal.getUserIdAsUUID(), deviceId);
            }

            if (!hasAccess) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }

            PredictionResponse predictions = predictionService.getPredictions(deviceId);
            return ResponseEntity.ok(ApiResponse.success(predictions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
