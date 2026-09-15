package com.fraud.transaction.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fraud.transaction.entity.Transaction;

@Service
public class TransactionProducer {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(Transaction transaction) {

        System.out.println(">>> sendTransaction() CALLED");

        String message = "{"
                + "\"id\":\"" + transaction.getId() + "\","
                + "\"customerId\":\"" + transaction.getCustomerId() + "\","
                + "\"amount\":" + transaction.getAmount() + ","
                + "\"currency\":\"" + transaction.getCurrency() + "\","
                + "\"merchant\":\"" + transaction.getMerchant() + "\","
                + "\"location\":\"" + transaction.getLocation() + "\","
                + "\"transactionType\":\"" + transaction.getTransactionType() + "\","
                + "\"deviceId\":\"" + transaction.getDeviceId() + "\","
                + "\"timestamp\":\"" + transaction.getTimestamp() + "\""
                + "}";

        try {

            kafkaTemplate.send(
                    "transaction-events",
                    transaction.getId(),
                    message
            ).get(10, java.util.concurrent.TimeUnit.SECONDS);

            System.out.println("KAFKA SEND SUCCESS: " + message);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            log.error("KAFKA SEND FAILED", e);

        } catch (java.util.concurrent.ExecutionException
                 | java.util.concurrent.TimeoutException e) {

            log.error("KAFKA SEND FAILED", e);
        }
    }
}