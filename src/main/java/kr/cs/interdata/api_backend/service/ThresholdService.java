package kr.cs.interdata.api_backend.service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.cs.interdata.api_backend.dto.*;
import kr.cs.interdata.api_backend.entity.AbnormalMetricLog;
import kr.cs.interdata.api_backend.entity.MetricsByType;
import kr.cs.interdata.api_backend.repository.MetricsByTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ThresholdService {

    // 클라이언트의 Emitter를 저장할 ConcurrentHashMap
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ThresholdService.class);

    private final AbnormalDetectionService abnormalDetectionService;
    private final MetricsByTypeRepository metricsByTypeRepository;

    @Autowired
    public ThresholdService(AbnormalDetectionService abnormalDetectionService,
                            MetricsByTypeRepository metricsByTypeRepository) {
        this.abnormalDetectionService = abnormalDetectionService;
        this.metricsByTypeRepository = metricsByTypeRepository;
    }

    // 1. 현재 설정된 임계값을 조회
    public ThresholdSetting getThreshold() {
        List<MetricsByType> hostMetrics = metricsByTypeRepository.findByType_Type("host");

        // 새로운 ThresholdSetting 객체 생성
        ThresholdSetting thresholdSetting = new ThresholdSetting();

        // 각 메트릭 이름에 따라 매핑
        for (MetricsByType metric : hostMetrics) {
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
                    break;
            }
        }
        return thresholdSetting;
    }

    // 2. 새로운 임계값을 설정
    public Map<String, String> setThreshold(ThresholdSetting dto) {
        // CPU 임계값 업데이트
        List<MetricsByType> cpuMetrics = metricsByTypeRepository.findByMetricName("cpu");
        cpuMetrics.forEach(metric -> metric.setThresholdValue(Double.valueOf(dto.getCpuPercent())));
        metricsByTypeRepository.saveAll(cpuMetrics);

        // Memory 임계값 업데이트
        List<MetricsByType> memoryMetrics = metricsByTypeRepository.findByMetricName("memory");
        memoryMetrics.forEach(metric -> metric.setThresholdValue(Double.valueOf(dto.getMemoryPercent())));
        metricsByTypeRepository.saveAll(memoryMetrics);

        // Disk 임계값 업데이트
        List<MetricsByType> diskMetrics = metricsByTypeRepository.findByMetricName("disk");
        diskMetrics.forEach(metric -> metric.setThresholdValue(Double.valueOf(dto.getDiskPercent())));
        metricsByTypeRepository.saveAll(diskMetrics);

        // Network Traffic 임계값 업데이트
        List<MetricsByType> networkMetrics = metricsByTypeRepository.findByMetricName("network");
        networkMetrics.forEach(metric -> metric.setThresholdValue(Double.valueOf(dto.getNetworkTraffic())));
        metricsByTypeRepository.saveAll(networkMetrics);

        // 응답 생성
        Map<String, String> response = new HashMap<>();
        response.put("message", "ok");

        return response;
    }

    /**
     * 3. 특정 날짜의 임계값 초과 이력 조회
     * @param date 조회할 날짜 정보
     * @return 이력 리스트
     */
    public List<Map<String, Object>> getThresholdHistory(DateforHistory date) {
        // Service를 통해 DB 조회
        List<AbnormalMetricLog> logs = abnormalDetectionService.getLatestAbnormalMetricsByDate(date.getDate());

        // 결과를 클라이언트에 맞게 매핑
        List<Map<String, Object>> result = new ArrayList<>();
        for (AbnormalMetricLog log : logs) {
            Map<String, Object> record = new HashMap<>();
            record.put("timestamp", log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            record.put("targetId", log.getTargetId());
            record.put("metricName", log.getMetricName());
            record.put("value", log.getValue().toString());
            result.add(record);
        }

        return result;
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
     *  5. threshold 조회
     *    -> consumer에서 임계값을 조회해 임계값을 넘어선 데이터가 있을 시,
     *          1. 이를 db에 저장한다.
     *          2. 이를
     *    -> db : AbnormalMetricLog, LatestAbnormalStatus
     * @param dto
     *        - typeId     : 메시지를 보낸 호스트
     *        - metricName   : 메트릭 이름
     *        - value    : 임계값을 넘은 값
     *        - timestamp: 임계값을 넘은 시각
     */
    public Object storeViolation(StoreViolation dto) {
        //이상값이 생긴 로그를 저장한다.
        abnormalDetectionService.storeViolation(
                dto.getTargetId(),
                dto.getMetricName(),
                dto.getValue(),
                dto.getTimestamp()
        );

        AlertThreshold alert = new AlertThreshold();
        alert.setTargetId(dto.getTargetId());
        alert.setMetricName(dto.getMetricName());
        alert.setValue(dto.getValue());
        alert.setTimestamp(dto.getTimestamp());

        // 실시간 전송
        publishThreshold(alert);

        //단, request에 대한 응답값은 없다.
        return "ok";
    }

    /**
     *  6. sse방식을 사용하기 위해 비동기로 emitter를 연결한다.
     *  -> SSE 연결을 생성하고 Emitter를 관리한다.
     *
     * @return emitter 연결
     */
    public SseEmitter alertThreshold() {
        String emitterId = "emitter_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Emitter 저장
        emitters.put(emitterId, emitter);

        // 연결이 끊어지면 맵에서 제거
        // 즉, 클라이언트가 페이지를 벗어나거나 연결을 끊으면, SseEmitter의 콜백이 실행됨.
        emitter.onCompletion(() -> emitters.remove(emitterId));
        emitter.onTimeout(() -> emitters.remove(emitterId));
        emitter.onError((e) -> emitters.remove(emitterId));

        logger.info("Client Connected: {}", emitterId);
        return emitter;
    }

    /**
     * 임계값을 초과한 데이터가 발생하면 실시간으로 전송한다.
     */
    public void publishThreshold(AlertThreshold alert) {
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(alert);
        } catch (IOException e) {
            // 🔸 변환에 실패하면 로깅만 하고 기본 메시지 설정
            logger.error("Failed to convert AlertThreshold to JSON. Sending default error message.", e);
            jsonData = "{\"error\": \"Failed to convert AlertThreshold to JSON\"}";
        }

        // 모든 Emitter에 전송
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(jsonData);
            } catch (IOException e) {
                logger.warn("Failed to send data to client. Removing emitter: " + entry.getKey());
                entry.getValue().completeWithError(e);
                emitters.remove(entry.getKey());
            }
        }
    }
}
