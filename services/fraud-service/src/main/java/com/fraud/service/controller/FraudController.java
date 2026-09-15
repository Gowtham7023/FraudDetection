package com.fraud.service.controller;

import com.fraud.service.dto.FraudRequest;
import com.fraud.service.dto.FraudResponse;
import com.fraud.service.fraud.FraudDetectionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/analyze")
    public FraudResponse analyzeTransaction(
            @RequestBody FraudRequest request) {

        return fraudDetectionService.analyze(request);
    }
}