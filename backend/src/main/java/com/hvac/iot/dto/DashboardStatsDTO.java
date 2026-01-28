package com.hvac.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalDevices;
    private long activeDevices;
    private long totalDeviceOwners;
    private long totalDeviceUsers;
    private long totalTeams;
    private long totalFaults;
    private long unresolvedFaults;
    private long licensedDevices;
    private long unlicensedDevices;
}
