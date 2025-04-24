package kr.cs.interdata.consumer.repository;

import kr.cs.interdata.consumer.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TargetTypeRepository extends JpaRepository<TargetType, Integer> {
    Optional<TargetType> findByType(String type);
}
