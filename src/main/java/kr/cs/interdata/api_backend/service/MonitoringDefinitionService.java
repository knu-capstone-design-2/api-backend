package kr.cs.interdata.api_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.cs.interdata.api_backend.dto.ThresholdSetting;
import kr.cs.interdata.api_backend.entity.MetricsByType;
import kr.cs.interdata.api_backend.entity.TargetType;
import kr.cs.interdata.api_backend.repository.MetricsByTypeRepository;
import kr.cs.interdata.api_backend.repository.TargetTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Slf4j
@Service
public class MonitoringDefinitionService {

    private final Logger logger = LoggerFactory.getLogger(MonitoringDefinitionService.class);
    private final TargetTypeRepository targetTypeRepository;
    private final MetricsByTypeRepository metricsByTypeRepository;

    @Autowired
    public MonitoringDefinitionService(
            TargetTypeRepository targetTypeRepository,
            MetricsByTypeRepository metricsByTypeRepository) {
        this.targetTypeRepository = targetTypeRepository;
        this.metricsByTypeRepository = metricsByTypeRepository;
    }

    // 타입 등록 - targetType
    public void registerType(String type) {
        TargetType targetType = new TargetType();
        targetType.setType(type);
        targetTypeRepository.save(targetType);

        logger.info("Register type {} successfully.", type);
    }

    // 새로운 메트릭 등록
    public void saveMetric(String metricName, String unit, Double threshold){
        MetricsByType metric = new MetricsByType();
        metric.setMetricName(metricName);
        metric.setUnit(unit);
        metric.setThresholdValue(threshold);
        metricsByTypeRepository.save(metric);

        logger.info("Save new metric : {}({})", metricName, unit);
    }

    /**
     * 특정 타입의 메트릭 임계값을 ThresholdSetting으로 매핑하여 조회
     *
     * @param typeName "host" 또는 "container"와 같은 타입명
     * @return ThresholdSetting 객체
     */
    public ThresholdSetting findThresholdByType(String typeName) {
        List<MetricsByType> metrics = metricsByTypeRepository.findByType_Type(typeName);

        ThresholdSetting thresholdSetting = new ThresholdSetting();
        metrics.forEach(metric -> {
            switch (metric.getMetricName()) {
                case "cpu":
                    thresholdSetting.setCpuPercent(metric.getThresholdValue().toString());
                    break;
                case "memory":
                    thresholdSetting.setMemoryPercent(metric.getThresholdValue().toString());
                    break;
                case "disk":
                    thresholdSetting.setDiskPercent(metric.getThresholdValue().toString());
                    break;
                case "network":
                    thresholdSetting.setNetworkTraffic(metric.getThresholdValue().toString());
                    break;
                default:
                    logger.warn("Unknown metric name found: {}", metric.getMetricName());
            }
        });

        return thresholdSetting;
    }

    /**
     * - 특정 메트릭의 임계값 업데이트
     *
     * @param metricName 메트릭 이름 (cpu, memory, disk, network)
     * @param thresholdValue 업데이트할 임계값
     */
    public void updateThresholdByMetricName(String metricName, double thresholdValue) {
        // 해당 메트릭 이름을 가진 모든 MetricsByType 조회
        List<MetricsByType> metrics = metricsByTypeRepository.findByMetricName(metricName);

        // 각각의 임계값 업데이트
        metrics.forEach(metric -> metric.setThresholdValue(thresholdValue));

        // 일괄 저장
        metricsByTypeRepository.saveAll(metrics);

        logger.info("Updated threshold for {} metrics to {}", metricName, thresholdValue);
    }

}
