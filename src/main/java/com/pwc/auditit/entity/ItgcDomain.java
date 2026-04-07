package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "itgc_domains")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItgcDomain {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String code;

    private String name;

    private String description;

    @DBRef
    @Builder.Default
    private List<Control> controls = new ArrayList<>();
}
