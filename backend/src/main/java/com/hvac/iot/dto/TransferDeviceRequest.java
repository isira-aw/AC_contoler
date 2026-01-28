package com.hvac.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferDeviceRequest {
    @NotNull(message = "New owner ID is required")
    private UUID newOwnerId;
}
