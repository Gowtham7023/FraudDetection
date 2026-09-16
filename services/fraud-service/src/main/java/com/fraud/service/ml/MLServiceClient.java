package com.fraud.service.ml;

import com.fraud.service.dto.FraudRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MLServiceClient {

    private final RestTemplate restTemplate;

    public MLServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public double getMLRiskScore(FraudRequest transaction) {

        try {
            Map<String, Object> request = new HashMap<>();

            request.put(
                    "amount",
                    transaction.getAmount() != null
                            ? transaction.getAmount().doubleValue()
                            : 0.0
            );

            request.put(
                    "unknown_device",
                    transaction.getDeviceId() != null
                            && transaction.getDeviceId()
                                    .equalsIgnoreCase("UNKNOWN")
                            ? 1
                            : 0
            );

            request.put(
                    "unknown_location",
                    transaction.getLocation() != null
                            && transaction.getLocation()
                                    .equalsIgnoreCase("UNKNOWN")
                            ? 1
                            : 0
            );

            Map<String, Object> response =
                    restTemplate.postForObject(
                            "http://localhost:8000/predict",
                            request,
                            Map.class
                    );

            if (response != null
                    && response.get("ml_risk_score") != null) {

                return ((Number) response.get("ml_risk_score"))
                        .doubleValue();
            }

        } catch (Exception e) {

            System.out.println(
                    "ML SERVICE UNAVAILABLE: "
                            + e.getMessage()
            );
        }

        return 0.0;
    }
}