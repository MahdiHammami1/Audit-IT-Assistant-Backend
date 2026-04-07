package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.AppType;
import com.pwc.auditit.entity.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ApplicationResponse {
    private UUID id;
    private UUID missionId;
    private String name;
    private AppType type;
    private List<String> domains;
    private ApplicationStatus statut;
    private Instant createdAt;
}
