package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Report;
import com.pwc.auditit.entity.enums.ReportType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends MongoRepository<Report, UUID> {
    List<Report> findByMissionIdOrderByGeneratedAtDesc(UUID missionId);
    List<Report> findByMissionIdAndType(UUID missionId, ReportType type);
}
