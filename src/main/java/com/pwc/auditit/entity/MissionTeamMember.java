package com.pwc.auditit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "mission_team_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionTeamMember {

    @Id
    private UUID id;

    @DBRef
    private Mission mission;

    @DBRef
    private Profile user;
}
