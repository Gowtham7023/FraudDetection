package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

public class UnusualLocationRule implements FraudRule {

    @Override
    public int evaluate(Transaction transaction) {
        if (transaction.getLocation() != null
                && transaction.getLocation().equalsIgnoreCase("UNKNOWN")) {
            return 15;
        }

        return 0;
    }

    @Override
    public String getReason() {
        return "Unusual or unknown location detected";
    }
}