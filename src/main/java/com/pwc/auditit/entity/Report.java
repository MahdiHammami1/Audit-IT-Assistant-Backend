package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.ReportDocStatus;
import com.pwc.auditit.entity.enums.ReportType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report {

    @Id
    private UUID id;

    @DBRef
    private Mission mission;

    private ReportType type;

    private String title;

    @Builder.Default
    private Instant generatedAt = Instant.now();

    @DBRef
    private Profile generatedBy;

    @Builder.Default
    private ReportDocStatus statut = ReportDocStatus.BROUILLON;

    private String filePathWord;

    private String filePathPdf;

    @DBRef
    @Builder.Default
    private List<ReportSection> sections = new ArrayList<>();
}
