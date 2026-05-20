package com.pwc.auditit.service;

import com.pwc.auditit.dto.GenerateCdwReportFromUploadRequest;
import com.pwc.auditit.dto.GenerateCdwReportResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Interface for CDW report generation service
 */
public interface CdwReportService {
    
    /**
     * Generate a CDW report from an uploaded Excel file
     * 
     * @param file the uploaded Excel file
     * @param missionId the mission ID to link the report to
     * @return GenerateCdwReportResponse containing report details
     */
    GenerateCdwReportResponse generateFromUpload(MultipartFile file, String missionId);

    /**
     * Generate a CDW report from an uploaded Excel file in the requested format.
     *
     * @param file the uploaded Excel file
     * @param missionId the mission ID to link the report to
     * @param format output format: docx or pptx
     * @return GenerateCdwReportResponse containing report details
     */
    GenerateCdwReportResponse generateFromUpload(MultipartFile file, String missionId, String format);

    /**
     * Retrieve generated reports for a given missionId
     * @param missionId mission identifier
     * @return list of GenerateCdwReportResponse
     */
    List<com.pwc.auditit.dto.GenerateCdwReportResponse> getReportsByMissionId(String missionId);

    /**
     * Delete a report and its associated blob by reportId
     * @param reportId report identifier
     */
    void deleteReportById(String reportId);
}

