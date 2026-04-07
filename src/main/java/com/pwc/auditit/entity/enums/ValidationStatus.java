package com.pwc.auditit.entity.enums;
public enum ValidationStatus {
    EN_ATTENTE("En attente"), VALIDE("Validé"), MODIFIE("Modifié"), REJETE("Rejeté");
    private final String label;
    ValidationStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
