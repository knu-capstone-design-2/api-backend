package kr.cs.interdata.api_backend.repository;

import kr.cs.interdata.api_backend.entity.LatestAbnormalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestAbnormalStatusRepository extends JpaRepository<LatestAbnormalStatus, Integer> {

    Optional<LatestAbnormalStatus> findByTargetIdAndMetricName(String targetId, String metricName);
}
