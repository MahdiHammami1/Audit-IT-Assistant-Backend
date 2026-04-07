package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.FieldType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "control_fields")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlField {

    @Id
    private UUID id;

    @DBRef
    private Control control;

    private String label;

    private FieldType fieldType;

    @Builder.Default
    private Boolean isRequired = false;

    @Builder.Default
    private Integer orderIndex = 0;
}
