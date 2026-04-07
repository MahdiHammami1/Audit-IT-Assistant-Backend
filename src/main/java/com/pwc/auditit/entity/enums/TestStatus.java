package com.pwc.auditit.entity.enums;
public enum TestStatus {
    NON_TESTE("Non testé"), EN_COURS("En cours"), COMPLETE("Complété");
    private final String label;
    TestStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
