package com.fraud.alert_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AlertConsumer {

    @KafkaListener(
            topics = "fraud-alerts",
            groupId = "alert-service"
    )
    public void consumeAlert(String message) {

        System.out.println("=================================");
        System.out.println("ALERT SERVICE RECEIVED FRAUD ALERT");
        System.out.println(message);
        System.out.println("=================================");
    }
}
