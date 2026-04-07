package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.TestStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class TestResultResponse {
    private UUID id;
    private UUID missionId;
    private UUID applicationId;
    private String applicationName;
    private ControlResponse control;
    private TestStatus statut;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TestFieldValueResponse> fieldValues;
    private List<UploadedFileResponse> uploadedFiles;
    private EvaluationResponse evaluation;
}
