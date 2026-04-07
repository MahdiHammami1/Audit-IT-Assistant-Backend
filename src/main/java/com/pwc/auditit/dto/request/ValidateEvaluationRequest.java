package com.pwc.auditit.dto.request;

import com.pwc.auditit.entity.enums.ValidationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateEvaluationRequest {
    @NotNull
    private ValidationStatus validationStatus;
    private String validationComment;
}
