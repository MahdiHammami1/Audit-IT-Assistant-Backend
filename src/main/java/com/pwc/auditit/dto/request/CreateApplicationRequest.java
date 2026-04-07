package com.pwc.auditit.dto.request;

import com.pwc.auditit.entity.enums.AppType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateApplicationRequest {
    @NotBlank(message = "Application name is required")
    private String name;

    @NotNull(message = "Application type is required")
    private AppType type;

    private List<String> domains;
}
