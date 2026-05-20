package com.pwc.auditit.repository;

import com.pwc.auditit.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Report documents (CDW Report model)
 */
@Repository("cdwReportRepository")
public interface CdwReportRepository extends MongoRepository<Report, String> {
    
    /**
     * Find all reports for a specific mission
     */
    List<Report> findByMissionId(String missionId);
    
    /**
     * Find a report by its generation run ID
     */
    Optional<Report> findByGenerationRunId(String generationRunId);
}

