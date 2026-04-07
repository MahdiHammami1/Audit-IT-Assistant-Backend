package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.MissionStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class MissionSummaryResponse {
    private UUID missionId;
    private String societe;
    private String exercice;
    private ReportType typeRapport;
    private MissionStatus statut;
    private Integer progress;
    private long totalTests;
    private long completedTests;
    private long deficiencesMineures;
    private long deficiencesMajeures;
    private long pendingValidations;
}
