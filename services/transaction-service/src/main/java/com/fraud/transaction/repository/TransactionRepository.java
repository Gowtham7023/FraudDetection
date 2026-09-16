package com.fraud.transaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fraud.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCustomerId(String customerId);
}