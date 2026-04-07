package com.pwc.auditit.entity.enums;
public enum ApplicationStatus {
    EN_ATTENTE("En attente"), EN_COURS("En cours"), COMPLETE("Complété");
    private final String label;
    ApplicationStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
