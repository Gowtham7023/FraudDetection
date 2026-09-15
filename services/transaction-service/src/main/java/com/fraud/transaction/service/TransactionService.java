package com.fraud.transaction.service;

import com.fraud.transaction.entity.Transaction;
import com.fraud.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(java.time.LocalDateTime.now());
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id).orElse(null);
    }
}