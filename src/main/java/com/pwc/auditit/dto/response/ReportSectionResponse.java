package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class ReportSectionResponse {
    private UUID id;
    private String title;
    private String content;
    private Integer orderIndex;
    private Boolean isEditable;
}
