package com.pwc.auditit.entity.enums;
public enum MissionStatus {
    EN_ATTENTE("En attente"), EN_COURS("En cours"),
    RAPPORT_GENERE("Rapport généré"), CLOTURE("Clôturé");
    private final String label;
    MissionStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
