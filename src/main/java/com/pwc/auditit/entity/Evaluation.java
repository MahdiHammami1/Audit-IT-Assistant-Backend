package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.DeficiencyType;
import com.pwc.auditit.entity.enums.EvaluationResult;
import com.pwc.auditit.entity.enums.ValidationStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "evaluations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evaluation {

    @Id
    private UUID id;

    @DBRef
    private TestResult testResult;

    private EvaluationResult result;

    private String aiSummary;

    private DeficiencyType deficiencyType;

    private String impact;

    private Map<String, Object> recommendations;

    @Builder.Default
    private ValidationStatus validationStatus = ValidationStatus.EN_ATTENTE;

    @DBRef
    private Profile validatedBy;

    private Instant validatedAt;

    private String validationComment;

    @CreatedDate
    private Instant createdAt;
}
