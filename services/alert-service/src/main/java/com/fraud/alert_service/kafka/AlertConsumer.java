package com.fraud.alert_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AlertConsumer {

    private final List<String> alerts =
            Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(
            topics = "fraud-alerts",
            groupId = "alert-service"
    )
    public void consumeAlert(String message) {

        System.out.println("=================================");
        System.out.println("ALERT SERVICE RECEIVED FRAUD ALERT");
        System.out.println(message);
        System.out.println("=================================");

        // Store the alert so the dashboard can retrieve it
        alerts.add(message);
    }

    public List<String> getAlerts() {
        synchronized (alerts) {
            return new ArrayList<>(alerts);
        }
    }
}