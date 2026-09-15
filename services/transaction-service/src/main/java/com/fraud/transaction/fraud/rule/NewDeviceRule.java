package com.fraud.transaction.fraud.rule;

import com.fraud.transaction.entity.Transaction;

public class NewDeviceRule implements FraudRule {

    @Override
    public int evaluate(Transaction transaction) {
        if (transaction.getDeviceId() != null
                && transaction.getDeviceId().equalsIgnoreCase("UNKNOWN")) {
            return 20;
        }

        return 0;
    }

    @Override
    public String getReason() {
        return "New or unknown device detected";
    }
}