package com.hvac.iot.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateDeviceRequest {
    private String name;
    private UUID ownerId;
    private UUID teamId;
    private Boolean licenseActive;
}
