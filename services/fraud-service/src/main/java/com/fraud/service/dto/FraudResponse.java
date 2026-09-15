package com.fraud.service.dto;

import java.util.List;

public class FraudResponse {

    private int riskScore;
    private String riskLevel;
    private boolean flagged;
    private List<String> reasons;

    public FraudResponse(int riskScore, String riskLevel,
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