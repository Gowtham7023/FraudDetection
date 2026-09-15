package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

import java.math.BigDecimal;

public class HighAmountRule implements FraudRule {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("50000");

    @Override
    public int evaluate(Transaction transaction) {
        if (transaction.getAmount() != null
                && transaction.getAmount().compareTo(HIGH_AMOUNT) > 0) {
            return 30;
        }

        return 0;
    }

    @Override
    public String getReason() {
        return "High transaction amount";
    }
}