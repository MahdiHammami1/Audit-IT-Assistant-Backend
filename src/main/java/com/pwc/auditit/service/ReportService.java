package com.pwc.auditit.service;

import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.ReportResponse;
import com.pwc.auditit.dto.request.UpdateReportSectionRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    ReportResponse generateReport(UUID missionId, UUID currentUserId);
    ReportResponse getReport(UUID reportId);
    List<ReportResponse> getReportsByMission(UUID missionId);
    ReportResponse updateSection(UUID reportId, UUID sectionId, UpdateReportSectionRequest request);
    void finalizeReport(UUID reportId);
    byte[] exportReportAsDocx(UUID reportId);




}