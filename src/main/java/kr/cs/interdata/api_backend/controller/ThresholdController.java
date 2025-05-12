package kr.cs.interdata.api_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.cs.interdata.api_backend.dto.*;
import kr.cs.interdata.api_backend.service.ThresholdService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class ThresholdController {

    private final ThresholdService thresholdService;

    @Autowired
    public ThresholdController(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    @GetMapping("/metrics/threshold-setting")
    public ResponseEntity<?> getThreshold() {
        return ResponseEntity.ok(thresholdService.getThreshold());
    }

    @PostMapping("/metrics/threshold-setting")
    public ResponseEntity<?> setThreshold(@RequestBody ThresholdSetting dto) {
        return ResponseEntity.ok(thresholdService.setThreshold(dto));
    }

    @PostMapping("/metrics/threshold-history")
    public ResponseEntity<?> getThresholdHistory(@RequestBody DateforHistory date) {
        return ResponseEntity.ok(thresholdService.getThresholdHistory(date));
    }

    @GetMapping("/metrics/threshold-check")
    public ResponseEntity<?> checkThreshold() {
        return ResponseEntity.ok(thresholdService.checkThreshold());
    }

    @PostMapping("/violation-store")
    public ResponseEntity<?> storeViolation(@RequestBody StoreViolation dto) {
        return ResponseEntity.ok(thresholdService.storeViolation(dto));
    }

    @GetMapping("/metrics/threshold-alert")
    public SseEmitter alertThreshold() {
        return thresholdService.alertThreshold();
    }

}
