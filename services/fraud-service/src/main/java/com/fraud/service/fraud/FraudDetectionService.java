package com.fraud.service.fraud;

import com.fraud.service.client.TransactionHistoryClient;
import com.fraud.service.dto.CustomerTransaction;
import com.fraud.service.dto.FraudRequest;
import com.fraud.service.dto.FraudResponse;
import com.fraud.service.kafka.AlertProducer;
import com.fraud.service.ml.MLServiceClient;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudDetectionService {

    private final AlertProducer alertProducer;
    private final TransactionHistoryClient transactionHistoryClient;
    private final MLServiceClient mlServiceClient;

    public FraudDetectionService(
            AlertProducer alertProducer,
            TransactionHistoryClient transactionHistoryClient,
            MLServiceClient mlServiceClient) {

        this.alertProducer = alertProducer;
        this.transactionHistoryClient = transactionHistoryClient;
        this.mlServiceClient = mlServiceClient;
    }

    public FraudResponse analyze(FraudRequest transaction) {

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        // =========================
        // RULE ENGINE
        // =========================

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
                && transaction.getDeviceId()
                        .equalsIgnoreCase("UNKNOWN")) {

            riskScore += 20;
            reasons.add("New or unknown device detected");
        }

        // Unknown location rule
        if (transaction.getLocation() != null
                && transaction.getLocation()
                        .equalsIgnoreCase("UNKNOWN")) {

            riskScore += 15;
            reasons.add("Unusual or unknown location detected");
        }

        // =========================
        // CUSTOMER HISTORY
        // =========================

        List<CustomerTransaction> previousTransactions =
                transactionHistoryClient.getCustomerTransactions(
                        transaction.getCustomerId()
                );

        // Remove current transaction
        previousTransactions = previousTransactions.stream()
                .filter(previous ->
                        transaction.getId() == null
                                || !transaction.getId()
                                        .equals(previous.getId()))
                .toList();

        System.out.println(
                "Previous transactions for "
                        + transaction.getCustomerId()
                        + ": "
                        + previousTransactions.size()
        );

        // Transaction frequency rule
        if (previousTransactions.size() >= 3) {

            riskScore += 10;

            reasons.add(
                    "High transaction frequency detected"
            );
        }

        // Customer-specific unusual amount rule
        if (!previousTransactions.isEmpty()
                && transaction.getAmount() != null) {

            double totalAmount = 0;

            for (CustomerTransaction previous :
                    previousTransactions) {

                if (previous.getAmount() != null) {

                    totalAmount +=
                            previous.getAmount().doubleValue();
                }
            }

            double averageAmount =
                    totalAmount / previousTransactions.size();

            double currentAmount =
                    transaction.getAmount().doubleValue();

            if (currentAmount > averageAmount * 3) {

                riskScore += 20;

                reasons.add(
                        "Transaction amount is unusually high for this customer"
                );
            }
        }

        // Location history rule
        if (!previousTransactions.isEmpty()
                && transaction.getLocation() != null
                && !transaction.getLocation()
                        .equalsIgnoreCase("UNKNOWN")) {

            boolean newLocation = true;

            for (CustomerTransaction previous :
                    previousTransactions) {

                if (previous.getLocation() != null
                        && previous.getLocation()
                                .equalsIgnoreCase(
                                        transaction.getLocation())) {

                    newLocation = false;
                    break;
                }
            }

            if (newLocation) {

                riskScore += 15;

                reasons.add(
                        "New transaction location detected for this customer"
                );
            }
        }

        // Device history rule
        if (!previousTransactions.isEmpty()
                && transaction.getDeviceId() != null
                && !transaction.getDeviceId()
                        .equalsIgnoreCase("UNKNOWN")) {

            boolean newDevice = true;

            for (CustomerTransaction previous :
                    previousTransactions) {

                if (previous.getDeviceId() != null
                        && previous.getDeviceId()
                                .equalsIgnoreCase(
                                        transaction.getDeviceId())) {

                    newDevice = false;
                    break;
                }
            }

            if (newDevice) {

                riskScore += 15;

                reasons.add(
                        "New device detected for this customer"
                );
            }
        }

        // Rapid transaction rule
        if (transaction.getTimestamp() != null) {

            LocalDateTime currentTime =
                    transaction.getTimestamp();

            int recentTransactionCount = 0;

            for (CustomerTransaction previous :
                    previousTransactions) {

                if (previous.getTimestamp() != null) {

                    long secondsBetween = Math.abs(
                            Duration.between(
                                    previous.getTimestamp(),
                                    currentTime
                            ).getSeconds()
                    );

                    if (secondsBetween <= 300) {

                        recentTransactionCount++;
                    }
                }
            }

            if (recentTransactionCount >= 2) {

                riskScore += 20;

                reasons.add(
                        "Multiple transactions detected within a short time"
                );
            }
        }

        // =========================
        // MACHINE LEARNING
        // =========================

        double mlRiskScore =
                mlServiceClient.getMLRiskScore(transaction);

        System.out.println(
                "ML Risk Score: "
                        + mlRiskScore
        );

        /*
         * Combine rule engine and ML.
         *
         * 70% rule score
         * 30% ML score
         */
        double combinedScore =
                (riskScore * 0.70)
                        + (mlRiskScore * 0.30);

        riskScore = (int) Math.round(combinedScore);

        // Maximum score
        if (riskScore > 100) {

            riskScore = 100;
        }

        // =========================
        // RISK LEVEL
        // =========================

        String riskLevel;

        if (riskScore <= 30) {

            riskLevel = "LOW";

        } else if (riskScore <= 70) {

            riskLevel = "MEDIUM";

        } else {

            riskLevel = "HIGH";
        }

        boolean flagged =
                riskLevel.equals("HIGH");

        // =========================
        // FINAL RESULT
        // =========================

        System.out.println();
        System.out.println(
                "########################################"
        );
        System.out.println(
                "### FINAL FRAUD ANALYSIS RESULT ###"
        );
        System.out.println(
                "Customer ID : "
                        + transaction.getCustomerId()
        );
        System.out.println(
                "Amount      : "
                        + transaction.getAmount()
        );
        System.out.println(
                "Rule Score  : "
                        + riskScore
        );
        System.out.println(
                "ML Score    : "
                        + mlRiskScore
        );
        System.out.println(
                "Risk Level  : "
                        + riskLevel
        );
        System.out.println(
                "Flagged     : "
                        + flagged
        );
        System.out.println(
                "Send Alert  : "
                        + transaction.isSendAlert()
        );
        System.out.println(
                "Reasons     : "
                        + reasons
        );
        System.out.println(
                "########################################"
        );
        System.out.println();

        // =========================
        // SEND FRAUD ALERT
        // =========================

        if (flagged && transaction.isSendAlert()) {

            String alertMessage = "{"
                    + "\"customerId\":\""
                    + transaction.getCustomerId()
                    + "\","
                    + "\"amount\":"
                    + transaction.getAmount()
                    + ","
                    + "\"riskScore\":"
                    + riskScore
                    + ","
                    + "\"mlRiskScore\":"
                    + mlRiskScore
                    + ","
                    + "\"riskLevel\":\""
                    + riskLevel
                    + "\","
                    + "\"flagged\":"
                    + flagged
                    + ","
                    + "\"reasons\":"
                    + reasons
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