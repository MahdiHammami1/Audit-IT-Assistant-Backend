package com.pwc.auditit.repository;

import com.pwc.auditit.entity.ReportSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportSectionRepository extends MongoRepository<ReportSection, String> {
    List<ReportSection> findByReportIdOrderByOrderIndexAsc(String reportId);
}
