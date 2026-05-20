package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the response from FastAPI when generating a CDW report
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiGenerateCdwReportResponse {
    
    private String status;
    
    private String message;
}

