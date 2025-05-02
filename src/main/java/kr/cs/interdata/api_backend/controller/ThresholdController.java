package kr.cs.interdata.api_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.cs.interdata.api_backend.dto.*;
import kr.cs.interdata.api_backend.service.ThresholdService;

@RestController
@RequestMapping("/api/metrics")
public class ThresholdController {

    private final ThresholdService thresholdService;

    public ThresholdController(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    @GetMapping("/threshold-setting")
    public ResponseEntity<?> getThreshold() {
        return ResponseEntity.ok(thresholdService.getThreshold());
    }

    @PostMapping("/threshold-setting")
    public ResponseEntity<?> setThreshold(@RequestBody ThresholdSetting dto) {
        return ResponseEntity.ok(thresholdService.setThreshold(dto));
    }

    @PostMapping("/threshold-history")
    public ResponseEntity<?> getThresholdHistory(@RequestBody DateforHistory date) {
        return ResponseEntity.ok(thresholdService.getThresholdHistory(date));
    }

    @PostMapping("/threshold-check")
    public ResponseEntity<?> checkThreshold(@RequestBody ThresholdCheck dto) {
        return ResponseEntity.ok(thresholdService.checkThreshold(dto));
    }

}
