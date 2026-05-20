package com.pwc.auditit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateCdwFromGraphRequest {

    @NotBlank(message = "itemId is required")
    private String itemId;

    @NotNull(message = "missionId is required")
    private UUID missionId;

    private String format = "docx";
}
