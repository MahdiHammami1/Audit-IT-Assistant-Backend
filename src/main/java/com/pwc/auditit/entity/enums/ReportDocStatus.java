package com.pwc.auditit.entity.enums;
public enum ReportDocStatus {
    BROUILLON("Brouillon"), FINALISE("Finalisé"), PARTAGE("Partagé");
    private final String label;
    ReportDocStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
