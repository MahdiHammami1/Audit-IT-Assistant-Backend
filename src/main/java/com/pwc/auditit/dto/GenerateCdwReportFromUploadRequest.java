package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for CDW Report generation request from file upload
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCdwReportFromUploadRequest {
    
    private String missionId;
    
    private String reportName;
}

