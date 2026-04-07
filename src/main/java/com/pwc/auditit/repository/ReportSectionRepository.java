package com.pwc.auditit.repository;

import com.pwc.auditit.entity.ReportSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportSectionRepository extends MongoRepository<ReportSection, UUID> {
    List<ReportSection> findByReportIdOrderByOrderIndexAsc(UUID reportId);
}
