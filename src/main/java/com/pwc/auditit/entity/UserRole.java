package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.AppRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "user_roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRole {

    @Id
    private UUID id;

    @DBRef
    private Profile user;

    private AppRole role;
}
