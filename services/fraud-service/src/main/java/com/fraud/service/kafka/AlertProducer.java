package com.fraud.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class AlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AlertProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendAlert(String message) {

        try {

            String validJson = fixAlertJson(message);

            kafkaTemplate.send("fraud-alerts", validJson);

            System.out.println("=================================");
            System.out.println("FRAUD ALERT SENT TO KAFKA");
            System.out.println(validJson);
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("FAILED TO SEND FRAUD ALERT");
            e.printStackTrace();
        }
    }

    private String fixAlertJson(String message) {

        try {

            /*
             * If the message is already valid JSON,
             * return it directly.
             */
            objectMapper.readTree(message);

            return message;

        } catch (Exception ignored) {

            /*
             * The old FraudDetectionService creates
             * the reasons array like:
             *
             * [High transaction amount, Very high transaction amount]
             *
             * Convert the values into valid JSON strings.
             */

            int reasonsStart = message.indexOf("\"reasons\":[");
            int reasonsEnd = message.lastIndexOf("]}");

            if (reasonsStart == -1 || reasonsEnd == -1) {
                return message;
            }

            String beforeReasons =
                    message.substring(
                            0,
                            reasonsStart + "\"reasons\":[".length()
                    );

            String reasonsText =
                    message.substring(
                            reasonsStart + "\"reasons\":[".length(),
                            reasonsEnd
                    );

            String[] reasons =
                    reasonsText.split(",\\s*");

            StringBuilder fixedReasons =
                    new StringBuilder();

            for (int i = 0; i < reasons.length; i++) {

                String reason = reasons[i].trim();

                if (reason.isEmpty()) {
                    continue;
                }

                if (fixedReasons.length() > 0) {
                    fixedReasons.append(",");
                }

                fixedReasons
                        .append("\"")
                        .append(reason.replace("\"", "\\\""))
                        .append("\"");
            }

            return beforeReasons
                    + fixedReasons
                    + message.substring(reasonsEnd);
        }
    }
}