package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "report_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportSection {

    @Id
    private UUID id;

    @DBRef
    private Report report;

    private String title;

    private String content;

    @Builder.Default
    private Integer orderIndex = 0;

    @Builder.Default
    private Boolean isEditable = true;
}
