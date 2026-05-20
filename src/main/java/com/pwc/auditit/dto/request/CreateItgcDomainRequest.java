package com.pwc.auditit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItgcDomainRequest {

    @NotBlank(message = "Domain code is required")
    @Size(min = 2, max = 50, message = "Domain code should be between 2 and 50 characters")
    private String code;

    @NotBlank(message = "Domain name is required")
    @Size(min = 2, max = 100, message = "Domain name should be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description should not exceed 500 characters")
    private String description;
}

