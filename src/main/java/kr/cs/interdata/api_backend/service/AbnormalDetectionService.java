package kr.cs.interdata.api_backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import kr.cs.interdata.api_backend.entity.AbnormalMetricLog;
import kr.cs.interdata.api_backend.entity.LatestAbnormalStatus;
import kr.cs.interdata.api_backend.repository.AbnormalMetricLogRepository;
import kr.cs.interdata.api_backend.repository.LatestAbnormalStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AbnormalDetectionService {

    private final AbnormalMetricLogRepository abnormalMetricLogRepository;
    private final LatestAbnormalStatusRepository latestAbnormalStatusRepository;

    @Autowired
    public AbnormalDetectionService(
            AbnormalMetricLogRepository abnormalMetricLogRepository,
            LatestAbnormalStatusRepository latestAbnormalStatusRepository){
        this.abnormalMetricLogRepository = abnormalMetricLogRepository;
        this.latestAbnormalStatusRepository = latestAbnormalStatusRepository;
    }

    /**
     *  - 이상 로그 저장하는 메서드
     *  - 데이터를 AbnormalMetricLog에 저장. 동시에 LatestAbnormalStatus에 저장 또는 갱신한다.
     *
     * @param id        이상값이 생긴 머신의 고유 id
     * @param metric    이상값이 생긴 메트릭 이름
     * @param value     이상값
     * @param timestamp 이상값이 생긴 시각
     */
    public void storeViolation(String id, String metric, String value, LocalDateTime timestamp) {
        // 1. AbnormalMetricLog 저장
        AbnormalMetricLog abn = new AbnormalMetricLog();

        abn.setTargetId(id);
        abn.setMetricName(metric);
        abn.setValue(Double.valueOf(value));
        abn.setTimestamp(timestamp);
        abnormalMetricLogRepository.save(abn);

        // 2. LatestAbnormalStatus 저장 또는 갱신
        Optional<LatestAbnormalStatus> optional =
                latestAbnormalStatusRepository.findByTargetIdAndMetricName(id, metric);

        if (optional.isPresent()) {
            // 이미 존재하면: 업데이트만
            LatestAbnormalStatus existing = optional.get();
            existing.setValue(value);
            existing.setDetectedAt(timestamp);
            existing.setResolved(false);
            latestAbnormalStatusRepository.save(existing);
        } else {
            // 존재하지 않으면: 새로 저장
            LatestAbnormalStatus latest_abn = new LatestAbnormalStatus();
            latest_abn.setTargetId(id);
            latest_abn.setMetricName(metric);
            latest_abn.setValue(value);
            latest_abn.setDetectedAt(timestamp);
            latest_abn.setResolved(false);
            latestAbnormalStatusRepository.save(latest_abn);
        }
    }

    //최근 이상 상태 조회 - latestAbnormalStauts
    /**
     * 날짜 기준으로 임계값을 초과한 기록을 조회
     * @param targetDate 조회할 날짜 (yyyy-MM-dd)
     * @return 조회된 기록 리스트
     */
    public List<AbnormalMetricLog> getLatestAbnormalMetricsByDate(String targetDate) {
        // 받은 날짜를 LocalDate로 변환
        LocalDate date = LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 날짜의 시작 시간과 끝 시간 계산
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // DB 조회 (특정 날짜의 임계치 초과 기록만 가져옴)
        return abnormalMetricLogRepository.findByTimestampBetween(startOfDay, endOfDay);
    }

    //(선택)1달 이상 지난 로그 삭제 -> 둘 다
}
