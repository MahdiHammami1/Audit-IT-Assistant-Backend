package com.pwc.auditit.dto.request;

import com.pwc.auditit.entity.enums.ReportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMissionRequest {
    @NotBlank(message = "Société is required")
    private String societe;

    @NotBlank(message = "Exercice is required")
    private String exercice;

    @NotNull(message = "Type de rapport is required")
    private ReportType typeRapport;

    @NotNull(message = "Date de début is required")
    private LocalDate dateDebut;

    @NotNull(message = "Date de fin is required")
    private LocalDate dateFin;

    @NotNull(message = "Auditeur responsable is required")
    private UUID auditeurResponsableId;

    private String notes;

    private List<UUID> teamMemberIds;

    @Valid
    private List<CreateApplicationRequest> applications;
}
