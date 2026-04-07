package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.TestStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "test_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestResult {

    @Id
    private UUID id;

    @DBRef
    private Mission mission;

    @DBRef
    private Application application;

    @DBRef
    private Control control;

    @Builder.Default
    private TestStatus statut = TestStatus.NON_TESTE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    @Builder.Default
    private List<TestFieldValue> fieldValues = new ArrayList<>();

    @DBRef
    @Builder.Default
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    @DBRef
    private Evaluation evaluation;
}
