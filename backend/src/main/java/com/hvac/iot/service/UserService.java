package com.hvac.iot.service;

import com.hvac.iot.dto.*;
import com.hvac.iot.model.*;
import com.hvac.iot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final SuperAdminRepository superAdminRepository;
    private final DeviceOwnerRepository deviceOwnerRepository;
    private final DeviceUserRepository deviceUserRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    // SuperAdmin operations
    @Transactional
    public DeviceOwnerDTO createDeviceOwner(RegisterDeviceOwnerRequest request, UUID superAdminId) {
        if (deviceOwnerRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new RuntimeException("Email already exists");
        }

        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));

        DeviceOwner owner = DeviceOwner.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdBy(superAdmin)
                .build();

        owner = deviceOwnerRepository.save(owner);
        log.info("Created DeviceOwner: {}", owner.getEmail());

        return mapToDeviceOwnerDTO(owner);
    }

    public List<DeviceOwnerDTO> getAllDeviceOwners() {
        return deviceOwnerRepository.findAll().stream()
                .map(this::mapToDeviceOwnerDTO)
                .collect(Collectors.toList());
    }

    public DeviceOwnerDTO getDeviceOwnerById(UUID id) {
        DeviceOwner owner = deviceOwnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeviceOwner not found"));
        return mapToDeviceOwnerDTO(owner);
    }

    // DeviceOwner operations
    @Transactional
    public DeviceUserDTO createDeviceUser(RegisterDeviceUserRequest request, UUID ownerId) {
        if (deviceUserRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new RuntimeException("Email already exists");
        }

        DeviceOwner owner = deviceOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("DeviceOwner not found"));

        Set<Team> teams = new HashSet<>();
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            for (UUID teamId : request.getTeamIds()) {
                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
                if (!team.getOwner().getId().equals(ownerId)) {
                    throw new RuntimeException("Team does not belong to this owner: " + teamId);
                }
                teams.add(team);
            }
        }

        DeviceUser user = DeviceUser.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdBy(owner)
                .teams(teams)
                .build();

        user = deviceUserRepository.save(user);
        log.info("Created DeviceUser: {}", user.getEmail());

        return mapToDeviceUserDTO(user);
    }

    public List<DeviceUserDTO> getDeviceUsersByOwner(UUID ownerId) {
        return deviceUserRepository.findByCreatedById(ownerId).stream()
                .map(this::mapToDeviceUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceUserDTO updateDeviceUser(UUID userId, UpdateDeviceUserRequest request, UUID ownerId) {
        DeviceUser user = deviceUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("DeviceUser not found"));

        if (!user.getCreatedBy().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to update this user");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (deviceUserRepository.existsByEmail(request.getEmail().toLowerCase())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        if (request.getTeamIds() != null) {
            Set<Team> teams = new HashSet<>();
            for (UUID teamId : request.getTeamIds()) {
                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
                if (!team.getOwner().getId().equals(ownerId)) {
                    throw new RuntimeException("Team does not belong to this owner: " + teamId);
                }
                teams.add(team);
            }
            user.setTeams(teams);
        }

        user = deviceUserRepository.save(user);
        return mapToDeviceUserDTO(user);
    }

    @Transactional
    public void deleteDeviceUser(UUID userId, UUID ownerId) {
        DeviceUser user = deviceUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("DeviceUser not found"));

        if (!user.getCreatedBy().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to delete this user");
        }

        deviceUserRepository.delete(user);
        log.info("Deleted DeviceUser: {}", user.getEmail());
    }

    private DeviceOwnerDTO mapToDeviceOwnerDTO(DeviceOwner owner) {
        return DeviceOwnerDTO.builder()
                .id(owner.getId())
                .name(owner.getName())
                .email(owner.getEmail())
                .createdAt(owner.getCreatedAt())
                .deviceCount(owner.getDevices().size())
                .teamCount(owner.getTeams().size())
                .userCount(owner.getCreatedUsers().size())
                .build();
    }

    private DeviceUserDTO mapToDeviceUserDTO(DeviceUser user) {
        List<TeamDTO> teamDTOs = user.getTeams().stream()
                .map(this::mapToTeamDTO)
                .collect(Collectors.toList());

        return DeviceUserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .teams(teamDTOs)
                .build();
    }

    private TeamDTO mapToTeamDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .createdAt(team.getCreatedAt())
                .deviceId(team.getDevice() != null ? team.getDevice().getId() : null)
                .deviceName(team.getDevice() != null ? team.getDevice().getName() : null)
                .userCount(team.getUsers().size())
                .build();
    }
}
