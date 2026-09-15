package com.fraud.transaction.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fraud.transaction.entity.Transaction;
import com.fraud.transaction.kafka.TransactionProducer;
import com.fraud.transaction.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionProducer transactionProducer) {

        this.transactionRepository = transactionRepository;
        this.transactionProducer = transactionProducer;
    }

    public Transaction createTransaction(Transaction transaction) {

        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(java.time.LocalDateTime.now());
        }

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // Send transaction to Kafka
        transactionProducer.sendTransaction(savedTransaction);

        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id).orElse(null);
    }
}