package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "controls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Control {

    @Id
    private UUID id;

    @DBRef
    private ItgcDomain domain;

    @Indexed(unique = true)
    private String code;

    private String title;

    private String description;

    @Builder.Default
    private Integer orderIndex = 0;

    @DBRef
    @Builder.Default
    private List<ControlField> fields = new ArrayList<>();
}
