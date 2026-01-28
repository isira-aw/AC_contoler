package com.hvac.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUserDTO {
    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private List<TeamDTO> teams;
}
