package com.pwc.auditit.dto.request;

import com.pwc.auditit.entity.enums.MissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMissionStatusRequest {
    @NotNull
    private MissionStatus statut;
}
