package com.fraud.transaction.controller;

import com.fraud.transaction.entity.Transaction;
import com.fraud.transaction.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Transaction> getTransactionsByCustomer(
            @PathVariable String customerId) {

        return transactionService.getTransactionsByCustomer(customerId);
    }
}