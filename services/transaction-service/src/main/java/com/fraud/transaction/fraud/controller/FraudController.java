package com.fraud.transaction.fraud.controller;

import com.fraud.transaction.entity.Transaction;
import com.fraud.transaction.fraud.FraudDetectionService;
import com.fraud.transaction.fraud.result.FraudResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/analyze")
    public FraudResult analyzeTransaction(
            @RequestBody Transaction transaction) {

        return fraudDetectionService.analyze(transaction);
    }
}