package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for CDW Report generation response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCdwReportResponse {
    
    private String reportId;
    
    private String generationRunId;
    
    private String status;
    
    private String fileName;

    private String format;

    private String contentType;
    
    private String downloadUrl;
    
    private String message;

    private String missionId;

    private String type;

    private String sourceFileName;

    private String generatedAt;
}

