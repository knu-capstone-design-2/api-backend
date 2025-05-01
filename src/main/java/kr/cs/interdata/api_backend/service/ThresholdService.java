package kr.cs.interdata.api_backend.service;

import java.util.List;

import kr.cs.interdata.api_backend.dto.DateforHistory;
import kr.cs.interdata.api_backend.dto.ThresholdCheck;
import kr.cs.interdata.api_backend.dto.ThresholdSetting;

public class ThresholdService {

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

    // 4. threshold 조회 
    //    -> consumer에서 임계값을 조회해 임계값을 넘어선 데이터가 있을 시, 이를 처리하는 용도
    public Object checkThreshold(ThresholdCheck dto) {
        // TODO: DB에서 임계값 조회 및 경고 메세지 전달
        return new Object(); // 실제로는 어떤 값을 보내지 않고 이 폴더 내에서 처리 -> 임계값을 넘어선다면 클라이언트으로 경고를 보냄.
    }
}
