package com.hvac.iot.repository;

import com.hvac.iot.model.FaultLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FaultLogRepository extends JpaRepository<FaultLog, UUID> {

    List<FaultLog> findByDeviceIdOrderByTimestampDesc(String deviceId);

    List<FaultLog> findByDeviceIdAndResolvedFalseOrderByTimestampDesc(String deviceId);

    @Query("SELECT f FROM FaultLog f WHERE f.device.id = :deviceId " +
           "AND f.timestamp BETWEEN :from AND :to ORDER BY f.timestamp DESC")
    List<FaultLog> findByDeviceIdAndTimestampBetween(
            @Param("deviceId") String deviceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(f) FROM FaultLog f WHERE f.device.id = :deviceId AND f.resolved = false")
    long countUnresolvedByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT f FROM FaultLog f WHERE f.device.id = :deviceId " +
           "AND f.faultType = :faultType AND f.resolved = false " +
           "ORDER BY f.timestamp DESC")
    List<FaultLog> findUnresolvedByDeviceIdAndType(
            @Param("deviceId") String deviceId,
            @Param("faultType") String faultType);
}
