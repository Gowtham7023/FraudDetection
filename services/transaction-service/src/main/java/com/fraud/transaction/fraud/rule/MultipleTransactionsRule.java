package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

public class MultipleTransactionsRule implements FraudRule {

    @Override
    public int evaluate(Transaction transaction) {
        // Transaction history will be checked in a later step.
        // For now, no additional risk points are added.
        return 0;
    }

    @Override
    public String getReason() {
        return "Multiple transactions detected in a short period";
    }
}