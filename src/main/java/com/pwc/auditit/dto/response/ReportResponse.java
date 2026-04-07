package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.ReportDocStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ReportResponse {
    private UUID id;
    private UUID missionId;
    private ReportType type;
    private String title;
    private Instant generatedAt;
    private ProfileResponse generatedBy;
    private ReportDocStatus statut;
    private String filePathWord;
    private String filePathPdf;
    private List<ReportSectionResponse> sections;
}
