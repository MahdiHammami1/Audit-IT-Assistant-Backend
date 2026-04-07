package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.MissionStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class MissionResponse {
    private UUID id;
    private String societe;
    private String exercice;
    private ReportType typeRapport;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private ProfileResponse auditeurResponsable;
    private String notes;
    private MissionStatus statut;
    private Integer progress;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProfileResponse> teamMembers;
    private List<ApplicationResponse> applications;
}
