package com.fraud.transaction.fraud;

import com.fraud.transaction.entity.Transaction;
import com.fraud.transaction.fraud.result.FraudResult;
import com.fraud.transaction.fraud.rule.FraudRule;
import com.fraud.transaction.fraud.rule.HighAmountRule;
import com.fraud.transaction.fraud.rule.VeryHighAmountRule;
import com.fraud.transaction.fraud.rule.NewDeviceRule;
import com.fraud.transaction.fraud.rule.UnusualLocationRule;
import com.fraud.transaction.fraud.rule.MultipleTransactionsRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FraudDetectionService {

    private final List<FraudRule> rules;

    public FraudDetectionService() {
        this.rules = List.of(
                new HighAmountRule(),
                new VeryHighAmountRule(),
                new NewDeviceRule(),
                new UnusualLocationRule(),
                new MultipleTransactionsRule()
        );
    }

    public FraudResult analyze(Transaction transaction) {

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        for (FraudRule rule : rules) {
            int score = rule.evaluate(transaction);

            if (score > 0) {
                riskScore += score;
                reasons.add(rule.getReason());
            }
        }

        if (riskScore > 100) {
            riskScore = 100;
        }

        String riskLevel;

        if (riskScore <= 30) {
            riskLevel = "LOW";
        } else if (riskScore <= 70) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }

        boolean flagged = riskLevel.equals("HIGH");

        return new FraudResult(
                riskScore,
                riskLevel,
                flagged,
                reasons
        );
    }
}