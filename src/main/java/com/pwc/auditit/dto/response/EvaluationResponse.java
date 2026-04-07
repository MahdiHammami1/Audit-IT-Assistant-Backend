package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.DeficiencyType;
import com.pwc.auditit.entity.enums.EvaluationResult;
import com.pwc.auditit.entity.enums.ValidationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class EvaluationResponse {
    private UUID id;
    private UUID testResultId;
    private EvaluationResult result;
    private String aiSummary;
    private DeficiencyType deficiencyType;
    private String impact;
    private Map<String, Object> recommendations;
    private ValidationStatus validationStatus;
    private ProfileResponse validatedBy;
    private Instant validatedAt;
    private String validationComment;
    private Instant createdAt;
}
