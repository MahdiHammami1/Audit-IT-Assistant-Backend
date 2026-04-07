package com.pwc.auditit.dto.response;

import com.pwc.auditit.entity.enums.AppRole;
import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data @Builder
public class ProfileResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String initials;
    private String avatarUrl;
    private Set<AppRole> roles;
}
