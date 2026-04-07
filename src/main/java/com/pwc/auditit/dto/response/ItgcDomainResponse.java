package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ItgcDomainResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<ControlResponse> controls;
}
