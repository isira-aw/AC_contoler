package com.hvac.iot.repository;

import com.hvac.iot.model.DeviceOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceOwnerRepository extends JpaRepository<DeviceOwner, UUID> {
    Optional<DeviceOwner> findByEmail(String email);
    boolean existsByEmail(String email);
    List<DeviceOwner> findByCreatedById(UUID superAdminId);
}
