package com.pwc.auditit.entity;

import com.pwc.auditit.entity.enums.AppRole;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(collection = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    private String fullName;

    private String password;

    private String initials;

    private String avatarUrl;

    @Builder.Default
    private String verificationCode = null;

    @Builder.Default
    private boolean isVerified = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    public boolean hasRole(AppRole role) {
        return roles.stream().anyMatch(r -> r.getRole() == role);
    }
}
