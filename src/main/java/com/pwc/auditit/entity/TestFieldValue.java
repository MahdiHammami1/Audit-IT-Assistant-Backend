package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "test_field_values")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestFieldValue {

    @Id
    private UUID id;

    @DBRef
    private TestResult testResult;

    @DBRef
    private ControlField controlField;

    private String valueText;

    private BigDecimal valueNumber;

    private LocalDate valueDate;
}
