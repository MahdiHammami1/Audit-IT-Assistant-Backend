package com.pwc.auditit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ChatbotRequest {

    @NotBlank(message = "message is required")
    private String message;

    private String sessionId;

    @Min(1)
    @Max(20)
    private Integer topK = 5;

    private String userRole;

    private Map<String, Object> filters;
}
