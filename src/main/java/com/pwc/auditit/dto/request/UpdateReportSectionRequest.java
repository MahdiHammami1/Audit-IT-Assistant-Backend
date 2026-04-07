package com.pwc.auditit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateReportSectionRequest {
    @NotBlank
    private String content;
}
