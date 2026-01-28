package com.hvac.iot.config;

import com.hvac.iot.model.*;
import com.hvac.iot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SuperAdminRepository superAdminRepository;
    private final DeviceOwnerRepository deviceOwnerRepository;
    private final DeviceUserRepository deviceUserRepository;
    private final TeamRepository teamRepository;
    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final FaultLogRepository faultLogRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        // Check if any data exists in the database (more robust check)
        if (superAdminRepository.count() > 0 || deviceRepository.count() > 0 || teamRepository.count() > 0) {
            log.info("Database already contains data, skipping seed data initialization");
            log.info("To reinitialize, clear the database tables first");
            return;
        }

        log.info("Initializing seed data...");

        // Create SuperAdmin
        SuperAdmin superAdmin = SuperAdmin.builder()
                .name("System Administrator")
                .email("admin@hvac.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .build();
        superAdmin = superAdminRepository.save(superAdmin);
        log.info("Created SuperAdmin: admin@hvac.com / admin123");

        // Create Device Owners
        DeviceOwner owner1 = DeviceOwner.builder()
                .name("John Smith")
                .email("john@example.com")
                .passwordHash(passwordEncoder.encode("owner123"))
                .createdBy(superAdmin)
                .build();
        owner1 = deviceOwnerRepository.save(owner1);

        DeviceOwner owner2 = DeviceOwner.builder()
                .name("Sarah Johnson")
                .email("sarah@example.com")
                .passwordHash(passwordEncoder.encode("owner123"))
                .createdBy(superAdmin)
                .build();
        owner2 = deviceOwnerRepository.save(owner2);
        log.info("Created 2 Device Owners");

        // Create Teams
        Team team1 = Team.builder()
                .name("Building A - Floor 1")
                .owner(owner1)
                .build();
        team1 = teamRepository.save(team1);

        Team team2 = Team.builder()
                .name("Building A - Floor 2")
                .owner(owner1)
                .build();
        team2 = teamRepository.save(team2);

        Team team3 = Team.builder()
                .name("Building B - Main")
                .owner(owner2)
                .build();
        team3 = teamRepository.save(team3);

        Team team4 = Team.builder()
                .name("Building A - Lobby")
                .owner(owner1)
                .build();
        team4 = teamRepository.save(team4);
        log.info("Created 4 Teams");

        // Create Device Users
        Set<Team> user1Teams = new HashSet<>();
        user1Teams.add(team1);
        user1Teams.add(team2);
        user1Teams.add(team4);

        DeviceUser user1 = DeviceUser.builder()
                .name("Mike Wilson")
                .email("mike@example.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .createdBy(owner1)
                .teams(user1Teams)
                .build();
        user1 = deviceUserRepository.save(user1);

        Set<Team> user2Teams = new HashSet<>();
        user2Teams.add(team1);

        DeviceUser user2 = DeviceUser.builder()
                .name("Emily Davis")
                .email("emily@example.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .createdBy(owner1)
                .teams(user2Teams)
                .build();
        user2 = deviceUserRepository.save(user2);

        Set<Team> user3Teams = new HashSet<>();
        user3Teams.add(team3);

        DeviceUser user3 = DeviceUser.builder()
                .name("David Brown")
                .email("david@example.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .createdBy(owner2)
                .teams(user3Teams)
                .build();
        user3 = deviceUserRepository.save(user3);
        log.info("Created 3 Device Users");

        // Create Devices
        Device device1 = Device.builder()
                .id("YORK-001")
                .name("HVAC Unit 1 - Conference Room")
                .owner(owner1)
                .team(team1)
                .licenseActive(true)
                .lastHeartbeat(LocalDateTime.now())
                .powerStatus("ON")
                .mode("COOLING")
                .fanSpeed("MED")
                .temperatureSetpoint(22.0f)
                .build();
        device1 = deviceRepository.save(device1);

        Device device2 = Device.builder()
                .id("YORK-002")
                .name("HVAC Unit 2 - Open Office")
                .owner(owner1)
                .team(team2)
                .licenseActive(true)
                .lastHeartbeat(LocalDateTime.now())
                .powerStatus("ON")
                .mode("COOLING")
                .fanSpeed("HIGH")
                .temperatureSetpoint(23.0f)
                .build();
        device2 = deviceRepository.save(device2);

        Device device3 = Device.builder()
                .id("YORK-003")
                .name("HVAC Unit 3 - Server Room")
                .owner(owner2)
                .team(team3)
                .licenseActive(true)
                .lastHeartbeat(LocalDateTime.now())
                .powerStatus("ON")
                .mode("COOLING")
                .fanSpeed("HIGH")
                .temperatureSetpoint(18.0f)
                .build();
        device3 = deviceRepository.save(device3);

        Device device4 = Device.builder()
                .id("YORK-004")
                .name("HVAC Unit 4 - Lobby")
                .owner(owner1)
                .team(team4)
                .licenseActive(false)
                .lastHeartbeat(LocalDateTime.now().minusHours(2))
                .powerStatus("OFF")
                .mode("HEATING")
                .fanSpeed("LOW")
                .temperatureSetpoint(24.0f)
                .build();
        device4 = deviceRepository.save(device4);
        log.info("Created 4 Devices");

        // Generate mock telemetry data for last 7 days
        Device[] devices = {device1, device2, device3};
        for (Device device : devices) {
            generateMockTelemetry(device);
        }
        log.info("Generated mock telemetry data");

        // Create some fault logs
        createFaultLogs(device1, device2, device3);
        log.info("Created fault logs");

        log.info("=== Seed Data Initialization Complete ===");
        log.info("Login credentials:");
        log.info("  SuperAdmin: admin@hvac.com / admin123");
        log.info("  DeviceOwner 1: john@example.com / owner123");
        log.info("  DeviceOwner 2: sarah@example.com / owner123");
        log.info("  DeviceUser 1: mike@example.com / user123");
        log.info("  DeviceUser 2: emily@example.com / user123");
        log.info("  DeviceUser 3: david@example.com / user123");
    }

    private void generateMockTelemetry(Device device) {
        LocalDateTime now = LocalDateTime.now();
        float baseEnergy = 0;

        // Generate data points every 10 minutes for the last 7 days
        for (int i = 7 * 24 * 6; i >= 0; i--) {
            LocalDateTime timestamp = now.minusMinutes(i * 10L);

            float supplyTemp = 12 + random.nextFloat() * 8;  // 12-20°C
            float returnTemp = supplyTemp + 8 + random.nextFloat() * 4;  // Supply + 8-12°C
            float roomTemp = 20 + random.nextFloat() * 6;  // 20-26°C
            float humidity = 40 + random.nextFloat() * 30;  // 40-70%
            float outdoorTemp = 25 + random.nextFloat() * 15;  // 25-40°C
            float voltage = 220 + random.nextFloat() * 20;  // 220-240V
            float current = 8 + random.nextFloat() * 8;  // 8-16A
            float power = voltage * current / 1000;  // kW
            baseEnergy += power * (10.0f / 60.0f);  // Accumulate kWh

            boolean compressorStatus = random.nextFloat() > 0.2;  // 80% on
            String fanSpeed = new String[]{"LOW", "MED", "HIGH"}[random.nextInt(3)];
            float filterCondition = 10 + random.nextFloat() * 30;  // 10-40 Pa
            float refrigerantPressure = 80 + random.nextFloat() * 40;  // 80-120 psi

            Telemetry telemetry = Telemetry.builder()
                    .device(device)
                    .timestamp(timestamp)
                    .supplyTemp(supplyTemp)
                    .returnTemp(returnTemp)
                    .roomTemp(roomTemp)
                    .humidity(humidity)
                    .outdoorTemp(outdoorTemp)
                    .voltage(voltage)
                    .current(current)
                    .power(power)
                    .energy(baseEnergy)
                    .compressorStatus(compressorStatus)
                    .fanSpeed(fanSpeed)
                    .airflowStatus(true)
                    .filterCondition(filterCondition)
                    .refrigerantPressure(refrigerantPressure)
                    .build();

            telemetryRepository.save(telemetry);
        }
    }

    private void createFaultLogs(Device... devices) {
        LocalDateTime now = LocalDateTime.now();

        // Device 1 - Resolved overcurrent fault
        FaultLog fault1 = FaultLog.builder()
                .device(devices[0])
                .faultType("Overcurrent")
                .value("Current: 22.5A (threshold: 20A)")
                .timestamp(now.minusDays(3))
                .resolved(true)
                .resolvedAt(now.minusDays(3).plusHours(2))
                .build();
        faultLogRepository.save(fault1);

        // Device 1 - Unresolved filter choke
        FaultLog fault2 = FaultLog.builder()
                .device(devices[0])
                .faultType("Filter Choke")
                .value("Filter pressure: 55 Pa (threshold: 50 Pa)")
                .timestamp(now.minusHours(6))
                .resolved(false)
                .build();
        faultLogRepository.save(fault2);

        // Device 2 - Resolved phase failure
        FaultLog fault3 = FaultLog.builder()
                .device(devices[1])
                .faultType("Phase Failure")
                .value("Voltage: 195V (range: 200-250V)")
                .timestamp(now.minusDays(5))
                .resolved(true)
                .resolvedAt(now.minusDays(5).plusMinutes(30))
                .build();
        faultLogRepository.save(fault3);

        // Device 3 - Unresolved overheating
        FaultLog fault4 = FaultLog.builder()
                .device(devices[2])
                .faultType("Overheating")
                .value("Supply temp: 47°C (threshold: 45°C)")
                .timestamp(now.minusHours(2))
                .resolved(false)
                .build();
        faultLogRepository.save(fault4);
    }
}
