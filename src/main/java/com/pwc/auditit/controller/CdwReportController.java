package com.pwc.auditit.controller;

import com.pwc.auditit.dto.GenerateCdwReportResponse;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.model.Report;
import com.pwc.auditit.repository.CdwReportRepository;
import com.pwc.auditit.service.CdwReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * REST Controller for CDW Report generation endpoints
 * Handles Excel file upload and CDW report generation via FastAPI
 */
@Slf4j
@RestController
@RequestMapping("/cdw-reports")
@Tag(name = "CDW Reports", description = "CDW report generation from Excel files")
public class CdwReportController {

    private static final String POWERPOINT_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String WORD_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final CdwReportService cdwReportService;
    private final CdwReportRepository cdwReportRepository;

    public CdwReportController(CdwReportService cdwReportService, CdwReportRepository cdwReportRepository) {
        this.cdwReportService = cdwReportService;
        this.cdwReportRepository = cdwReportRepository;
    }

    /**
     * Generate a CDW report from an uploaded Excel file
     * 
     * Flow:
     * 1. Validates the Excel file (.xlsx or .xls)
     * 2. Sends to FastAPI (agents/cdw-report or agents/cdw-ppt-report) for processing
     * 3. Receives generated Word or PowerPoint report
     * 4. Stores in Blob Storage
     * 
     * @param file the Excel file to process
     * @param missionId the mission ID to link the report to
     * @param format output format: docx or pptx
     * @return GenerateCdwReportResponse with report details and download URL
     */
    @PostMapping(value = "/generate-from-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Generate CDW report from Excel file",
        description = "Upload an Excel file to generate a CDW report. The file is processed by FastAPI and stored in Blob Storage."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or missing missionId"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error generating report")
    })
    public ResponseEntity<ApiResponse<GenerateCdwReportResponse>> generateCdwReportFromUpload(
            @RequestPart(name = "file") MultipartFile file,
            @RequestParam(name = "missionId", required = true) String missionId,
            @RequestParam(name = "format", required = false, defaultValue = "docx") String format
    ) {
        log.info(
                "Received CDW report generation request for mission: {}, format: {}, file: {}",
                missionId,
                format,
                file.getOriginalFilename()
        );

        // Validate missionId
        if (missionId == null || missionId.isBlank()) {
            log.warn("missionId is missing or blank");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mission ID is required"));
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            log.warn("File is missing or empty");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is required and cannot be empty"));
        }

        String fileName = file.getOriginalFilename();
        if (!isValidExcelFile(file)) {
            log.warn("Invalid file type: {}. Only Excel files (.xlsx, .xls) are supported", fileName);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only Excel files (.xlsx, .xls) are supported"));
        }

        try {
            log.debug("Processing Excel file: {} for mission: {}", fileName, missionId);
            GenerateCdwReportResponse response = cdwReportService.generateFromUpload(file, missionId, format);

            // Replace blob public URL with internal secure download endpoint
            if (response != null && response.getReportId() != null) {
                String secureDownloadPath = "/api/cdw-reports/download/" + response.getReportId();
                response.setDownloadUrl(secureDownloadPath);
            }

            log.info("CDW report generated successfully. Report ID: {}, Download URL: {}",
                    response.getReportId(), response.getDownloadUrl());

            return ResponseEntity.ok(ApiResponse.ok(
                    "CDW report generated successfully", 
                    response
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for CDW report generation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error generating CDW report from file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate CDW report: " + e.getMessage()));
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all CDW reports", description = "Retrieve all generated CDW report metadata for the admin console")
    public ResponseEntity<ApiResponse<List<GenerateCdwReportResponse>>> getAllReports() {
        List<GenerateCdwReportResponse> reports = cdwReportRepository.findAll().stream()
                .map(this::mapReport)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Reports retrieved", reports));
    }

    /**
     * Retrieve generated CDW reports for a given missionId
     */
    @GetMapping(value = "/by-mission/{missionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get CDW reports by missionId", description = "Retrieve generated CDW reports for a given missionId")
    public ResponseEntity<ApiResponse<List<GenerateCdwReportResponse>>> getReportsByMissionId(
            @PathVariable("missionId") String missionId
    ) {
        log.info("Fetching CDW reports for missionId={}", missionId);

        if (missionId == null || missionId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("missionId is required"));
        }

        try {
            List<GenerateCdwReportResponse> reports = cdwReportService.getReportsByMissionId(missionId);
            return ResponseEntity.ok(ApiResponse.ok("Reports retrieved", reports));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request when fetching reports: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching reports for missionId={}", missionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to fetch reports: " + e.getMessage()));
        }
    }

    /**
     * Delete a generated CDW report by its ID
     */
    @DeleteMapping(value = "/{reportId}")
    @Operation(summary = "Delete CDW report", description = "Delete a generated CDW report and its stored file by reportId")
    public ResponseEntity<ApiResponse<String>> deleteReportById(
            @PathVariable("reportId") String reportId
    ) {
        log.info("Request to delete reportId={}", reportId);

        if (reportId == null || reportId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("reportId is required"));
        }

        try {
            cdwReportService.deleteReportById(reportId);
            return ResponseEntity.ok(ApiResponse.ok("Report deleted", reportId));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delete request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting report {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete report: " + e.getMessage()));
        }
    }

    /**
     * Validate if the file is a valid Excel file
     */
    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        return (contentType != null && (
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/x-xlsx") ||
                contentType.equals("application/x-xls")
        )) || (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")));
    }

    private GenerateCdwReportResponse mapReport(Report report) {
        GenerateCdwReportResponse response = new GenerateCdwReportResponse();
        response.setReportId(report.getId());
        response.setGenerationRunId(report.getGenerationRunId());
        response.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
        response.setFileName(report.getGeneratedFileName());
        response.setFormat(resolveReportFormat(report));
        response.setContentType(resolveReportContentType(report));
        response.setDownloadUrl("/api/cdw-reports/download/" + report.getId());
        response.setMissionId(report.getMissionId());
        response.setType(report.getType() != null ? report.getType().name() : null);
        response.setSourceFileName(report.getSourceFileName());
        response.setGeneratedAt(report.getCreatedAt() != null ? report.getCreatedAt().toString() : null);
        return response;
    }

    private String resolveReportFormat(Report report) {
        if (report.getFormat() != null && !report.getFormat().isBlank()) {
            return report.getFormat();
        }

        String fileName = report.getGeneratedFileName();
        if (fileName != null && fileName.toLowerCase().endsWith(".pptx")) {
            return "pptx";
        }
        return "docx";
    }

    private String resolveReportContentType(Report report) {
        if (report.getContentType() != null && !report.getContentType().isBlank()) {
            return report.getContentType();
        }
        return "pptx".equals(resolveReportFormat(report)) ? POWERPOINT_CONTENT_TYPE : WORD_CONTENT_TYPE;
    }
}

