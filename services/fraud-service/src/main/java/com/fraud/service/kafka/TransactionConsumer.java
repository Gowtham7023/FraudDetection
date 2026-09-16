package com.fraud.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fraud.service.dto.FraudRequest;
import com.fraud.service.dto.FraudResponse;
import com.fraud.service.fraud.FraudDetectionService;

import tools.jackson.databind.ObjectMapper;

@Service
public class TransactionConsumer {

    private final FraudDetectionService fraudDetectionService;

    private final ObjectMapper objectMapper;

    public TransactionConsumer(
            FraudDetectionService fraudDetectionService,
            ObjectMapper objectMapper) {

        this.fraudDetectionService = fraudDetectionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "transaction-events",
            groupId = "fraud-service"
    )
    public void consumeTransaction(String message) {

        try {

            System.out.println("=================================");
            System.out.println("FRAUD SERVICE RECEIVED TRANSACTION");
            System.out.println(message);

            FraudRequest transaction =
                    objectMapper.readValue(message, FraudRequest.class);

            // Transactions received through Kafka are real new transactions.
            // They are allowed to generate fraud alerts.
            transaction.setSendAlert(true);

            System.out.println(
                    "Transaction Timestamp: "
                            + transaction.getTimestamp()
            );

            System.out.println(
                    "Send Alert: "
                            + transaction.isSendAlert()
            );

            FraudResponse result =
                    fraudDetectionService.analyze(transaction);

            System.out.println("FRAUD ANALYSIS RESULT");
            System.out.println("Risk Score: " + result.getRiskScore());
            System.out.println("Risk Level: " + result.getRiskLevel());
            System.out.println("Flagged: " + result.isFlagged());
            System.out.println("Reasons: " + result.getReasons());
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("FRAUD ANALYSIS FAILED");
            e.printStackTrace();
        }
    }
}