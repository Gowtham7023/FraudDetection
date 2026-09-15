package com.fraud.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AlertProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAlert(String message) {

        kafkaTemplate.send(
                "fraud-alerts",
                message
        );

        System.out.println("FRAUD ALERT SENT TO KAFKA");
        System.out.println(message);
    }
} 