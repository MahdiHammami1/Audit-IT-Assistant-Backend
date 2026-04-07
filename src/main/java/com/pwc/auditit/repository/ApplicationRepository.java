package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, UUID> {
    List<Application> findByMissionIdOrderByCreatedAtAsc(UUID missionId);
    long countByMissionId(UUID missionId);
}
