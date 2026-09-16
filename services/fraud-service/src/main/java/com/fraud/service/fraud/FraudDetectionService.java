package com.fraud.service.fraud;

import com.fraud.service.client.TransactionHistoryClient;
import com.fraud.service.dto.CustomerTransaction;
import com.fraud.service.dto.FraudRequest;
import com.fraud.service.dto.FraudResponse;
import com.fraud.service.kafka.AlertProducer;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudDetectionService {

    private final AlertProducer alertProducer;
    private final TransactionHistoryClient transactionHistoryClient;

    public FraudDetectionService(
            AlertProducer alertProducer,
            TransactionHistoryClient transactionHistoryClient) {

        this.alertProducer = alertProducer;
        this.transactionHistoryClient = transactionHistoryClient;
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

        // Get customer's previous transactions
        List<CustomerTransaction> previousTransactions =
                transactionHistoryClient.getCustomerTransactions(
                        transaction.getCustomerId()
                );

        // Remove the current transaction from history
        previousTransactions = previousTransactions.stream()
                .filter(previous -> transaction.getId() == null
                        || !transaction.getId().equals(previous.getId()))
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
            reasons.add("High transaction frequency detected");
        }

        // Customer-specific unusual amount rule
        if (!previousTransactions.isEmpty()
                && transaction.getAmount() != null) {

            double totalAmount = 0;

            for (CustomerTransaction previous : previousTransactions) {

                if (previous.getAmount() != null) {
                    totalAmount += previous.getAmount().doubleValue();
                }
            }

            double averageAmount =
                    totalAmount / previousTransactions.size();

            double currentAmount =
                    transaction.getAmount().doubleValue();

            // Current transaction is more than 3 times customer's average
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
                && !transaction.getLocation().equalsIgnoreCase("UNKNOWN")) {

            boolean newLocation = true;

            for (CustomerTransaction previous : previousTransactions) {

                if (previous.getLocation() != null
                        && previous.getLocation().equalsIgnoreCase(
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
                && !transaction.getDeviceId().equalsIgnoreCase("UNKNOWN")) {

            boolean newDevice = true;

            for (CustomerTransaction previous : previousTransactions) {

                if (previous.getDeviceId() != null
                        && previous.getDeviceId().equalsIgnoreCase(
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

            System.out.println(
                    "Current transaction timestamp: "
                            + currentTime
            );

            for (CustomerTransaction previous : previousTransactions) {

                if (previous.getTimestamp() != null) {

                    System.out.println(
                            "Previous transaction timestamp: "
                                    + previous.getTimestamp()
                    );

                    long secondsBetween = Math.abs(
                            Duration.between(
                                    previous.getTimestamp(),
                                    currentTime
                            ).getSeconds()
                    );

                    System.out.println(
                            "Seconds between transactions: "
                                    + secondsBetween
                    );

                    // Previous transaction occurred within 5 minutes
                    if (secondsBetween <= 300) {
                        recentTransactionCount++;
                    }
                }
            }

            System.out.println(
                    "Recent transactions within 5 minutes: "
                            + recentTransactionCount
            );

            // 2 or more previous transactions within 5 minutes
            if (recentTransactionCount >= 2) {

                riskScore += 20;

                reasons.add(
                        "Multiple transactions detected within a short time"
                );

                System.out.println(
                        ">>> RAPID TRANSACTION RULE TRIGGERED <<<"
                );

                System.out.println(
                        ">>> Added 20 Risk Score <<<"
                );
            }
        }

        // Maximum score is 100
        if (riskScore > 100) {
            riskScore = 100;
        }

        // Determine risk level
        String riskLevel;

        if (riskScore <= 30) {

            riskLevel = "LOW";

        } else if (riskScore <= 70) {

            riskLevel = "MEDIUM";

        } else {

            riskLevel = "HIGH";
        }

        boolean flagged = riskLevel.equals("HIGH");

        // Final fraud analysis result
        System.out.println();
        System.out.println("########################################");
        System.out.println("### FINAL FRAUD ANALYSIS RESULT ###");
        System.out.println("Customer ID : " + transaction.getCustomerId());
        System.out.println("Amount      : " + transaction.getAmount());
        System.out.println("Risk Score  : " + riskScore);
        System.out.println("Risk Level  : " + riskLevel);
        System.out.println("Flagged     : " + flagged);
        System.out.println("Send Alert  : " + transaction.isSendAlert());
        System.out.println("Reasons     : " + reasons);
        System.out.println("########################################");
        System.out.println();

        // Send Kafka alert ONLY when:
        // 1. Transaction is HIGH risk
        // 2. Transaction came from the real Kafka transaction flow
        if (flagged && transaction.isSendAlert()) {

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