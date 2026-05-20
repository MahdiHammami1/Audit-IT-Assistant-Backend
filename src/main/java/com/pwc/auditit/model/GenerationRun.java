package com.pwc.auditit.model;


import com.pwc.auditit.entity.enums.GenerationStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing a CDW report generation run
 */
@Document(collection = "generation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationRun {

    @Id
    private String id;

    private String missionId;

    private ReportType reportType;

    private GenerationStatus status;

    private String sourceFileName;

    private String sourceFileHash;

    private Long sourceFileSize;

    private String sourceContentType;

    private String outputFormat;

    private String outputContentType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String errorMessage;
}

