package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "uploaded_files")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UploadedFile {

    @Id
    private UUID id;

    @DBRef
    private TestResult testResult;

    @DBRef
    private ControlField controlField;

    private String fileName;

    private String filePath;

    private Integer fileSize;

    private String mimeType;

    @DBRef
    private Profile uploadedBy;

    @Builder.Default
    private Instant uploadedAt = Instant.now();
}
