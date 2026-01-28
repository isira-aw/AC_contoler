package com.hvac.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterDeviceRequest {
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String name;

    private UUID ownerId;
    private UUID teamId;
}
