package kr.cs.interdata.api_backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThresholdHistory {

    private LocalDateTime timestamp;

    private String targetId;    // 호스트, 컨테이너 구분 가능
    private String metricName;  // cpu, memory, disk, network
    private String value;       // 임계치 초과했을 때의 값
    
}
