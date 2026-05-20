package com.pwc.auditit.repository;

import com.pwc.auditit.model.GenerationRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for GenerationRun documents
 */
@Repository
public interface GenerationRunRepository extends MongoRepository<GenerationRun, String> {
    
    /**
     * Find all generation runs for a specific mission
     */
    List<GenerationRun> findByMissionId(String missionId);
}

