package kr.cs.interdata.api_backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.cs.interdata.api_backend.dto.DateforHistory;
import kr.cs.interdata.api_backend.dto.StoreViolation;
import kr.cs.interdata.api_backend.dto.ThresholdSetting;
import kr.cs.interdata.api_backend.entity.MetricsByType;
import kr.cs.interdata.api_backend.repository.MetricsByTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {

    private final AbnormalDtectionService abnormalDtectionService;
    private final MetricsByTypeRepository metricsByTypeRepository;

    @Autowired
    public ThresholdService(AbnormalDtectionService abnormalDtectionService,
                            MetricsByTypeRepository metricsByTypeRepository) {
        this.abnormalDtectionService = abnormalDtectionService;
        this.metricsByTypeRepository = metricsByTypeRepository;
    }

    // 1. 현재 설정된 임계값을 조회
    public Object getThreshold() {
        // TODO: 현재 임계값을 DB에서 조회
        return new Object(); // 실제로는 DTO를 반환
    }

    // 2. 새로운 임계값을 설정
    public Object setThreshold(ThresholdSetting dto) {
        /* 
            ThresholdSetting thresholds = new ThresholdSetting(
            dto.getCpuPercent(),
            dto.getMemoryPercent(),
            dto.getDiskPercent(),
            dto.getNetworkTraffic()
        )
        */
        // TODO: 유효성 검사 → DB 저장
        return new Object(); // 실제로는 응답 상태 메시지 : "ok"
    }

    // 3. 임계값을 초과한 기록 조회, 날짜 기준 필터링
    public List<Object> getThresholdHistory(DateforHistory date) {
        // TODO: DB에서 특정 날짜의 이력 리스트 조회
        return List.of(); // 실제로는 이력 DTO 리스트
    }

    /**
     *  4. threshold 조회
     *  -> MetricsByType 테이블의 모든 값을 조회해, 
     *      모든 타입의 모든 metric의 threshold를 Map의 형태로 저장하여 return한다.
     * 
     * @return Map<type(String), Map<metric_name(String), threshold(Double)>> resultMap
     *      ex. <host, <cpu, 80.0>>, <container,<memory,95.0>>, ...
     */
    public Object checkThreshold() {
        // Key는 'host' 또는 'container', Value는 해당 타입에 대한 메트릭들에 대한 Map을 선언한다.
        Map<String, Map<String, Double>> resultMap = new ConcurrentHashMap<>();

        // 모든 MetricsByType 데이터를 가져옴.
        List<MetricsByType> metricsList = metricsByTypeRepository.findAll();

        // 데이터를 순차적으로 처리하여 결과 Map에 추가한다.
        for (MetricsByType metric : metricsList) {
            // type (host 또는 container)을 key로 사용
            String typeKey = metric.getType().getType();

            // 메트릭 이름과 threshold 값을 Map에 추가
            Map<String, Double> metricMap = resultMap.getOrDefault(typeKey, new ConcurrentHashMap<>());
            metricMap.put(metric.getMetricName(), metric.getThresholdValue());

            //결과 Map
            resultMap.put(typeKey, metricMap);
        }

        // 최종적으로 resultMap을 반환
        return resultMap;
    }

    /**
     * TODO: 테스트 전 메서드 - service/ThresholdService.java - 5.threshold 조회
     *
     *  5. threshold 조회
     *    -> consumer에서 임계값을 조회해 임계값을 넘어선 데이터가 있을 시, 이를 db에 저장한다.
     *    -> db : AbnormalMetricLog, LatestAbnormalStatus
     * @param dto
     *        - type     : 메시지를 보낸 호스트
     *        - metric   : 메트릭 이름
     *        - value    : 임계값을 넘은 값
     *        - timestamp: 임계값을 넘은 시각
     */
    public String storeViolation(StoreViolation dto) {
        abnormalDtectionService.storeViolation(
                dto.getMachineId(),
                dto.getMetric(),
                dto.getValue(),
                dto.getTimestamp()
        );

        return "ok";
    }
}
