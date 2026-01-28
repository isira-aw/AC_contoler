package com.hvac.iot.repository;

import com.hvac.iot.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {

    List<Telemetry> findByDeviceIdOrderByTimestampDesc(String deviceId);

    @Query("SELECT t FROM Telemetry t WHERE t.device.id = :deviceId " +
           "AND t.timestamp BETWEEN :from AND :to ORDER BY t.timestamp ASC")
    List<Telemetry> findByDeviceIdAndTimestampBetween(
            @Param("deviceId") String deviceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    Optional<Telemetry> findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    @Query("SELECT AVG(t.power) FROM Telemetry t WHERE t.device.id = :deviceId " +
           "AND t.timestamp >= :since")
    Double getAveragePowerSince(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);

    @Query("SELECT SUM(t.energy) FROM Telemetry t WHERE t.device.id = :deviceId " +
           "AND t.timestamp >= :since")
    Double getTotalEnergySince(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM Telemetry t WHERE t.device.id = :deviceId " +
           "AND t.compressorStatus = true AND t.timestamp >= :since")
    Long getCompressorRuntimeCountSince(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);

    @Modifying
    @Transactional
    @Query("DELETE FROM Telemetry t WHERE t.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
