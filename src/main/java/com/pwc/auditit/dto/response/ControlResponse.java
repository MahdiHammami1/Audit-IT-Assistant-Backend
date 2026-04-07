package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ControlResponse {
    private UUID id;
    private String domainCode;
    private String code;
    private String title;
    private String description;
    private Integer orderIndex;
    private List<ControlFieldResponse> fields;
}
