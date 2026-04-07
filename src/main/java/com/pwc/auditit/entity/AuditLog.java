package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    private UUID id;

    @DBRef
    private Profile user;

    private String action;

    private String entityType;

    private UUID entityId;

    private Map<String, Object> details;

    @CreatedDate
    private Instant createdAt;
}
