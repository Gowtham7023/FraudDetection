package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

import java.math.BigDecimal;

public class VeryHighAmountRule implements FraudRule {

    private static final BigDecimal VERY_HIGH_AMOUNT = new BigDecimal("100000");

    @Override
    public int evaluate(Transaction transaction) {
        if (transaction.getAmount() != null
                && transaction.getAmount().compareTo(VERY_HIGH_AMOUNT) >= 0) {
            return 50;
        }

        return 0;
    }

    @Override
    public String getReason() {
        return "Very high transaction amount";
    }
}