package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.MissionStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Document(collection = "missions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mission {

    @Id
    private UUID id;

    private String societe;

    private String exercice;

    private ReportType typeRapport;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @DBRef
    private Profile auditeurResponsable;

    private String notes;

    @Builder.Default
    private MissionStatus statut = MissionStatus.EN_ATTENTE;

    @Builder.Default
    private Integer progress = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    @Builder.Default
    private Set<MissionTeamMember> teamMembers = new HashSet<>();

    @DBRef
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    @DBRef
    @Builder.Default
    private List<Report> reports = new ArrayList<>();
}
