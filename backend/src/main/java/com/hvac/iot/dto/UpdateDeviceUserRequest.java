package com.hvac.iot.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateDeviceUserRequest {
    private String name;
    private String email;
    private List<UUID> teamIds;
}
