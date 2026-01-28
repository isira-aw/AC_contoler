package com.hvac.iot.repository;

import com.hvac.iot.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByOwnerId(UUID ownerId);
    Page<Device> findByOwnerId(UUID ownerId, Pageable pageable);
    List<Device> findByTeamId(UUID teamId);

    @Query("SELECT d FROM Device d WHERE d.team.id IN " +
           "(SELECT t.id FROM Team t JOIN t.users u WHERE u.id = :userId)")
    List<Device> findDevicesForUser(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Device d " +
           "WHERE d.id = :deviceId AND d.team.id IN " +
           "(SELECT t.id FROM Team t JOIN t.users u WHERE u.id = :userId)")
    boolean canUserAccessDevice(@Param("userId") UUID userId, @Param("deviceId") String deviceId);
}
