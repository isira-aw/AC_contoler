package com.hvac.iot.repository;

import com.hvac.iot.model.DeviceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceUserRepository extends JpaRepository<DeviceUser, UUID> {
    Optional<DeviceUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<DeviceUser> findByCreatedById(UUID ownerId);

    @Query("SELECT du FROM DeviceUser du JOIN du.teams t WHERE t.id = :teamId")
    List<DeviceUser> findByTeamId(@Param("teamId") UUID teamId);
}
