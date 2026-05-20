package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private UUID id;
    private Instant timestamp;
    private String userFullName;
    private String userEmail;
    private String action;
    private String module;
    private String status;
    private Map<String, Object> details;
}
