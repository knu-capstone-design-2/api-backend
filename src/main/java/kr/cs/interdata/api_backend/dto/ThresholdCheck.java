package kr.cs.interdata.api_backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThresholdCheck {

    private String host;    // 메시지를 보낸 호스트
    private String metric;  // 메트릭 이름
    private String value;   // 임계값을 넘은 값
    private LocalDateTime timestamp;    // 임계값을 넘은 시각

    
}
