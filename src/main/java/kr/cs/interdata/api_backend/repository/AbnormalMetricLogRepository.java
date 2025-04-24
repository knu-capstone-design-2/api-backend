package kr.cs.interdata.api_backend.repository;

import kr.cs.interdata.api_backend.entity.AbnormalMetricLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbnormalMetricLogRepository extends JpaRepository<AbnormalMetricLog, Integer> {
}
