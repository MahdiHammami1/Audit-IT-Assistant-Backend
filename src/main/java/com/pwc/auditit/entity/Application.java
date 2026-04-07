package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.AppType;
import com.pwc.auditit.entity.enums.ApplicationStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Application {

    @Id
    private UUID id;

    @DBRef
    private Mission mission;

    private String name;

    private AppType type;

    @Builder.Default
    private List<String> domains = new ArrayList<>();

    @Builder.Default
    private ApplicationStatus statut = ApplicationStatus.EN_ATTENTE;

    @CreatedDate
    private Instant createdAt;

    @DBRef
    @Builder.Default
    private List<TestResult> testResults = new ArrayList<>();
}
