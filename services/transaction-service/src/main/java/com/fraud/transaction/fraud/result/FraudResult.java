package com.fraud.transaction.fraud.result;

import java.util.List;

public class FraudResult {

    private int riskScore;
    private String riskLevel;
    private boolean flagged;
    private List<String> reasons;

    public FraudResult(int riskScore, String riskLevel,
                       boolean flagged, List<String> reasons) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.flagged = flagged;
        this.reasons = reasons;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public List<String> getReasons() {
        return reasons;
    }
}