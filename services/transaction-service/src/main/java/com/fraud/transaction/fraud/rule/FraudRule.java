package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

public interface FraudRule {

    int evaluate(Transaction transaction);

    String getReason();
}