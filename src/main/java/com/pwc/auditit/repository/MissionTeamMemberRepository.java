package com.pwc.auditit.repository;

import com.pwc.auditit.entity.MissionTeamMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionTeamMemberRepository extends MongoRepository<MissionTeamMember, UUID> {
    List<MissionTeamMember> findByMissionId(UUID missionId);
    boolean existsByMissionIdAndUserId(UUID missionId, UUID userId);
    void deleteByMissionIdAndUserId(UUID missionId, UUID userId);
}
