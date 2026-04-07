package com.pwc.auditit.entity.enums;
public enum EvaluationResult {
    CONFORME("Conforme"), DEFICIENCE_MINEURE("Déficience mineure"), DEFICIENCE_MAJEURE("Déficience majeure");
    private final String label;
    EvaluationResult(String label) { this.label = label; }
    public String getLabel() { return label; }
}
