package com.fraud.service.fraud;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fraud.service.dto.FraudRequest;
import com.fraud.service.dto.FraudResponse;
import com.fraud.service.kafka.AlertProducer;

@Service
public class FraudDetectionService {

    private final AlertProducer alertProducer;

    public FraudDetectionService(AlertProducer alertProducer) {
        this.alertProducer = alertProducer;
    }

    public FraudResponse analyze(FraudRequest transaction) {

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        // High amount rule
        if (transaction.getAmount() != null
                && transaction.getAmount().doubleValue() > 50000) {

            riskScore += 30;
            reasons.add("High transaction amount");
        }

        // Very high amount rule
        if (transaction.getAmount() != null
                && transaction.getAmount().doubleValue() >= 100000) {

            riskScore += 50;
            reasons.add("Very high transaction amount");
        }

        // Unknown device rule
        if (transaction.getDeviceId() != null
                && transaction.getDeviceId().equalsIgnoreCase("UNKNOWN")) {

            riskScore += 20;
            reasons.add("New or unknown device detected");
        }

        // Unknown location rule
        if (transaction.getLocation() != null
                && transaction.getLocation().equalsIgnoreCase("UNKNOWN")) {

            riskScore += 15;
            reasons.add("Unusual or unknown location detected");
        }

        // Maximum score is 100
        if (riskScore > 100) {
            riskScore = 100;
        }

        String riskLevel;

        if (riskScore <= 30) {
            riskLevel = "LOW";
        } else if (riskScore <= 70) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }

        boolean flagged = riskLevel.equals("HIGH");

        // Send alert to Kafka only for HIGH-risk transactions
        if (flagged) {

            String alertMessage = "{"
                    + "\"customerId\":\"" + transaction.getCustomerId() + "\","
                    + "\"amount\":" + transaction.getAmount() + ","
                    + "\"riskScore\":" + riskScore + ","
                    + "\"riskLevel\":\"" + riskLevel + "\","
                    + "\"flagged\":" + flagged + ","
                    + "\"reasons\":" + reasons
                    + "}";

            alertProducer.sendAlert(alertMessage);
        }

        return new FraudResponse(
                riskScore,
                riskLevel,
                flagged,
                reasons
        );
    }
}