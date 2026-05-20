package com.pwc.auditit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateControlRequest {

    @NotBlank(message = "Domain code is required")
    private String domainCode;

    @NotEmpty(message = "Controls list cannot be empty")
    private List<CreateControlRequest> controls;
}

