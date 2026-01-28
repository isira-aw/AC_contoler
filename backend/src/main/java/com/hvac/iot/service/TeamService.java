package com.hvac.iot.service;

import com.hvac.iot.dto.CreateTeamRequest;
import com.hvac.iot.dto.TeamDTO;
import com.hvac.iot.model.DeviceOwner;
import com.hvac.iot.model.Team;
import com.hvac.iot.repository.DeviceOwnerRepository;
import com.hvac.iot.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final DeviceOwnerRepository deviceOwnerRepository;

    @Transactional
    public TeamDTO createTeam(CreateTeamRequest request, UUID ownerId) {
        DeviceOwner owner = deviceOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("DeviceOwner not found"));

        if (teamRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new RuntimeException("Team with this name already exists");
        }

        Team team = Team.builder()
                .name(request.getName())
                .owner(owner)
                .build();

        team = teamRepository.save(team);
        log.info("Created team: {} for owner: {}", team.getName(), owner.getEmail());

        return mapToTeamDTO(team);
    }

    public List<TeamDTO> getTeamsByOwner(UUID ownerId) {
        return teamRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToTeamDTO)
                .collect(Collectors.toList());
    }

    public TeamDTO getTeamById(UUID teamId, UUID ownerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to access this team");
        }

        return mapToTeamDTO(team);
    }

    @Transactional
    public void deleteTeam(UUID teamId, UUID ownerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to delete this team");
        }

        if (team.getDevice() != null) {
            throw new RuntimeException("Cannot delete team with assigned device. Remove the device assignment first.");
        }

        teamRepository.delete(team);
        log.info("Deleted team: {}", team.getName());
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
