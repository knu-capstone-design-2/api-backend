package kr.cs.interdata.api_backend.repository;

import kr.cs.interdata.api_backend.entity.MetricsByType;
import kr.cs.interdata.api_backend.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetricsByTypeRepository extends JpaRepository<MetricsByType, Integer> {

    // metricName으로 엔티티를 찾아주는 메서드
    Optional<MetricsByType> findByMetricName(String metricName);

    // type과 metricName으로 엔티티를 찾아주는 메서드
    Optional<MetricsByType> findByType_TypeAndMetricName(String type, String metricName);

    boolean existsByTypeAndMetricName(TargetType type, String metricName);
}
