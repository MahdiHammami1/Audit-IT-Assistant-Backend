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
 * MongoDB document representing a generated CDW report
 */
@Document(collection = "cdw_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    private String id;

    private String missionId;

    private ReportType type;

    private String generationRunId;

    private String sourceFileName;

    private String sourceFileHash;

    private Long sourceFileSize;

    private String generatedFileName;

    private String format;

    private String contentType;

    private String blobPath;

    private String fileUrl;


    private GenerationStatus status;

    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

