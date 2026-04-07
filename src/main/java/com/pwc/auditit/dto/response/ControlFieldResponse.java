package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.FieldType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class ControlFieldResponse {
    private UUID id;
    private String label;
    private FieldType fieldType;
    private Boolean isRequired;
    private Integer orderIndex;
}
