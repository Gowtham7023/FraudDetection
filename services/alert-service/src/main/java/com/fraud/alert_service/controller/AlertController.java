package com.fraud.alert_service.controller;

import com.fraud.alert_service.kafka.AlertConsumer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:5173")
public class AlertController {

    private final AlertConsumer alertConsumer;

    public AlertController(AlertConsumer alertConsumer) {
        this.alertConsumer = alertConsumer;
    }

    @GetMapping
    public List<String> getAlerts() {
        return alertConsumer.getAlerts();
    }
}


