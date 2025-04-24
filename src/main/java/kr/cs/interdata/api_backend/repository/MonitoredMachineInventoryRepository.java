package kr.cs.interdata.consumer.repository;

import kr.cs.interdata.consumer.entity.MonitoredMachineInventory;
import kr.cs.interdata.consumer.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonitoredMachineInventoryRepository extends JpaRepository<MonitoredMachineInventory, String> {

    @Override
    Optional<MonitoredMachineInventory> findById(String s);

    long countByType(TargetType type);
}
