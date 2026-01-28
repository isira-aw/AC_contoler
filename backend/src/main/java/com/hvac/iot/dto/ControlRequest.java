package com.hvac.iot.dto;

import lombok.Data;

@Data
public class ControlRequest {
    private String status;   // ON, OFF
    private String mode;     // COOLING, HEATING
    private String speed;    // LOW, MED, HIGH
    private Float setpoint;  // Temperature setpoint
}
