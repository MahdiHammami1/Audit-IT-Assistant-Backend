package com.pwc.auditit.service.impl;

import com.pwc.auditit.client.FastApiClient;
import com.pwc.auditit.client.FastApiClient.FastApiGeneratedFile;
import com.pwc.auditit.dto.BlobUploadResult;
import com.pwc.auditit.dto.GenerateCdwReportResponse;

import com.pwc.auditit.entity.enums.GenerationStatus;
import com.pwc.auditit.entity.enums.ReportType;
import com.pwc.auditit.model.GenerationRun;
import com.pwc.auditit.model.Report;
import com.pwc.auditit.repository.CdwReportRepository;
import com.pwc.auditit.repository.GenerationRunRepository;
import com.pwc.auditit.service.BlobStorageService;
import com.pwc.auditit.service.CdwReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Implementation of CDW report generation service
 * Handles the complete flow from Excel upload to generated report storage
 */
@Slf4j
@Service
public class CdwReportServiceImpl implements CdwReportService {

    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String FORMAT_DOCX = "docx";
    private static final String FORMAT_PPTX = "pptx";
    private static final String WORD_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String POWERPOINT_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    private final FastApiClient fastApiClient;
    private final BlobStorageService blobStorageService;
    private final GenerationRunRepository generationRunRepository;
    private final CdwReportRepository cdwReportRepository;

    public CdwReportServiceImpl(FastApiClient fastApiClient,
                               BlobStorageService blobStorageService,
                               GenerationRunRepository generationRunRepository,
                               CdwReportRepository cdwReportRepository) {
        this.fastApiClient = fastApiClient;
        this.blobStorageService = blobStorageService;
        this.generationRunRepository = generationRunRepository;
        this.cdwReportRepository = cdwReportRepository;
    }

    /**
     * Main method to generate a CDW report from an uploaded Excel file
     * Flow:
     * 1. Validate and extract metadata from Excel file
     * 2. Create GenerationRun with PENDING status
     * 3. Send Excel to FastAPI for processing
     * 4. Upload generated report file to Blob Storage
     * 5. Create Report record in MongoDB
     * 6. Update GenerationRun to COMPLETED
     */
    @Override
    public GenerateCdwReportResponse generateFromUpload(MultipartFile file, String missionId) {
        return generateFromUpload(file, missionId, FORMAT_DOCX);
    }

    @Override
    public GenerateCdwReportResponse generateFromUpload(MultipartFile file, String missionId, String format) {
        String outputFormat = normalizeOutputFormat(format);
        String outputContentType = contentTypeForFormat(outputFormat);
        log.info("Starting CDW {} report generation for mission: {}", outputFormat.toUpperCase(), missionId);

        // Step 1: Validate and extract file metadata
        validateFile(file);
        String sourceFileName = Objects.requireNonNull(file.getOriginalFilename());
        String sourceFileHash = computeSha256(file);
        Long sourceFileSize = file.getSize();
        String contentType = file.getContentType();

        // Step 2: Create GenerationRun with PENDING status (store missionId)
        GenerationRun generationRun = createGenerationRun(
                missionId,
                sourceFileName,
                sourceFileHash,
                sourceFileSize,
                contentType,
                outputFormat,
                outputContentType
        );

        try {
            // Update status to IN_PROGRESS
            generationRun.setStatus(GenerationStatus.IN_PROGRESS);
            generationRun.setUpdatedAt(LocalDateTime.now());
            generationRunRepository.save(generationRun);

            // Step 3: Send Excel to FastAPI
            log.debug("Sending Excel file to FastAPI: {}", sourceFileName);
            FastApiGeneratedFile generatedReportFile = fastApiClient.generateCdwReportFileFromExcel(file, missionId, outputFormat);
            String generatedFileName = resolveGeneratedFileName(sourceFileName, outputFormat, generatedReportFile.fileName());
            String generatedContentType = hasText(generatedReportFile.contentType())
                    ? generatedReportFile.contentType()
                    : outputContentType;

            // Step 4: Upload report file to Blob Storage
            log.debug("Uploading generated {} file to Blob Storage: {}", outputFormat.toUpperCase(), generatedFileName);
            BlobUploadResult uploadResult = blobStorageService.uploadFile(
                    generatedReportFile.content(),
                    generatedFileName,
                    missionId,
                    generatedContentType
            );

            // Step 5: Create Report in MongoDB
            Report report = createReport(
                    missionId,
                    generationRun.getId(),
                    sourceFileName,
                    sourceFileHash,
                    sourceFileSize,
                    generatedFileName,
                    outputFormat,
                    generatedContentType,
                    uploadResult
            );
            Report savedReport = cdwReportRepository.save(report);

            indexGeneratedReportForChatbot(savedReport, generatedContentType);

            // Step 6: Update GenerationRun to COMPLETED
            generationRun.setStatus(GenerationStatus.COMPLETED);
            generationRun.setUpdatedAt(LocalDateTime.now());
            generationRunRepository.save(generationRun);

            log.info("CDW report generation completed successfully. Report ID: {}, Generation Run ID: {}", savedReport.getId(), generationRun.getId());

            return mapToResponse(savedReport, generationRun, "CDW report generated successfully");

        } catch (Exception e) {
            log.error("Error during CDW report generation", e);

            // Update GenerationRun to FAILED
            generationRun.setStatus(GenerationStatus.FAILED);
            generationRun.setErrorMessage(e.getMessage());
            generationRun.setUpdatedAt(LocalDateTime.now());
            generationRunRepository.save(generationRun);

            throw new RuntimeException("Failed to generate CDW report: " + e.getMessage(), e);
        }
    }

    /**
     * Validate the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (!isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only Excel files (.xlsx, .xls) are supported");
        }
    }

    /**
     * Check if the file is a valid Excel file
     */
    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        return (contentType != null && (
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel")
        )) || (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")));
    }

    /**
     * Compute SHA-256 hash of the file
     */
    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_SHA256);
            byte[] fileBytes = file.getBytes();
            byte[] hashBytes = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error computing file hash", e);
            throw new RuntimeException("Failed to compute file hash: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a filename for the report.
     * Format: {source}_CDW_Report_{timestamp}.{format}
     */
    private String generateFileName(String sourceFileName, String outputFormat) {
        String baseName = sourceFileName.replaceAll("\\.[^.]+$", "");
        long timestamp = System.currentTimeMillis();
        return baseName + "_CDW_Report_" + timestamp + "." + outputFormat;
    }

    private String resolveGeneratedFileName(String sourceFileName, String outputFormat, String fastApiFileName) {
        if (hasText(fastApiFileName)) {
            String sanitized = fastApiFileName.replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
            if (sanitized.toLowerCase().endsWith("." + outputFormat)) {
                return sanitized;
            }
            return sanitized + "." + outputFormat;
        }

        return generateFileName(sourceFileName, outputFormat);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeOutputFormat(String format) {
        if (format == null || format.isBlank()) {
            return FORMAT_DOCX;
        }

        String normalized = format.trim().toLowerCase();
        if (normalized.equals("ppt") || normalized.equals(FORMAT_PPTX) || normalized.equals("powerpoint")) {
            return FORMAT_PPTX;
        }
        if (normalized.equals("doc") || normalized.equals(FORMAT_DOCX) || normalized.equals("word")) {
            return FORMAT_DOCX;
        }

        throw new IllegalArgumentException("Unsupported report format. Use docx or pptx");
    }

    private String contentTypeForFormat(String outputFormat) {
        return FORMAT_PPTX.equals(outputFormat) ? POWERPOINT_CONTENT_TYPE : WORD_CONTENT_TYPE;
    }

    /**
     * Create a GenerationRun entity
     */
    private GenerationRun createGenerationRun(
            String missionId,
            String sourceFileName,
            String sourceFileHash,
            Long sourceFileSize,
            String contentType,
            String outputFormat,
            String outputContentType
    ) {
        LocalDateTime now = LocalDateTime.now();
        GenerationRun generationRun = GenerationRun.builder()
                .missionId(missionId)
                .reportType(ReportType.CDW_REPORT)
                .status(GenerationStatus.PENDING)
                .sourceFileName(sourceFileName)
                .sourceFileHash(sourceFileHash)
                .sourceFileSize(sourceFileSize)
                .sourceContentType(contentType)
                .outputFormat(outputFormat)
                .outputContentType(outputContentType)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return generationRunRepository.save(generationRun);
    }

    /**
     * Create a Report entity
     */
    private Report createReport(
            String missionId,
            String generationRunId,
            String sourceFileName,
            String sourceFileHash,
            Long sourceFileSize,
            String generatedFileName,
            String outputFormat,
            String outputContentType,
            BlobUploadResult uploadResult
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Report.builder()
                .missionId(missionId)
                .type(com.pwc.auditit.entity.enums.ReportType.CDW_REPORT)
                .generationRunId(generationRunId)
                .sourceFileName(sourceFileName)
                .sourceFileHash(sourceFileHash)
                .sourceFileSize(sourceFileSize)
                .generatedFileName(generatedFileName)
                .format(outputFormat)
                .contentType(outputContentType)
                .blobPath(uploadResult.getBlobPath())
                .fileUrl(uploadResult.getFileUrl())
                .status(com.pwc.auditit.entity.enums.GenerationStatus.COMPLETED)
                .version(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void indexGeneratedReportForChatbot(Report report, String contentType) {
        if (report.getBlobPath() == null || report.getBlobPath().isBlank()) {
            return;
        }

        try {
            fastApiClient.indexGeneratedReportBlob(
                    report.getBlobPath(),
                    report.getId(),
                    report.getMissionId(),
                    report.getGeneratedFileName(),
                    contentType
            );
            log.info("Queued generated report in RAG index. reportId={}, blobPath={}", report.getId(), report.getBlobPath());
        } catch (Exception e) {
            log.warn(
                    "Generated report was saved, but RAG indexing failed. reportId={}, blobPath={}, error={}",
                    report.getId(),
                    report.getBlobPath(),
                    e.getMessage()
            );
        }
    }

    /**
     * Map Report and GenerationRun entities to response DTO
     */
    private GenerateCdwReportResponse mapToResponse(Report report, GenerationRun generationRun, String message) {
        GenerateCdwReportResponse response = new GenerateCdwReportResponse();
        response.setReportId(report.getId());
        // generationRun can be null when the report was created in a previous flow
        String generationRunId = generationRun != null ? generationRun.getId() : report.getGenerationRunId();
        String status = GenerationStatus.PENDING.name();
        if (generationRun != null && generationRun.getStatus() != null) {
            status = generationRun.getStatus().name();
        } else if (report.getStatus() != null) {
            status = report.getStatus().name();
        }

        response.setGenerationRunId(generationRunId);
        response.setStatus(status);
        response.setFileName(report.getGeneratedFileName());
        response.setFormat(resolveReportFormat(report));
        response.setContentType(resolveReportContentType(report));
        response.setDownloadUrl(report.getId() != null ? "/api/cdw-reports/download/" + report.getId() : report.getFileUrl());
        response.setMessage(message);
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
            return FORMAT_PPTX;
        }
        return FORMAT_DOCX;
    }

    private String resolveReportContentType(Report report) {
        if (report.getContentType() != null && !report.getContentType().isBlank()) {
            return report.getContentType();
        }
        return contentTypeForFormat(resolveReportFormat(report));
    }

    @Override
    public List<GenerateCdwReportResponse> getReportsByMissionId(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("missionId is required");
        }

        List<Report> reports = cdwReportRepository.findByMissionId(missionId);

        return reports.stream().map(report -> {
            GenerationRun generationRun = null;
            if (report.getGenerationRunId() != null) {
                generationRun = generationRunRepository.findById(report.getGenerationRunId()).orElse(null);
            }
            return mapToResponse(report, generationRun, "Report retrieved");
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteReportById(String reportId) {
        if (reportId == null || reportId.isBlank()) {
            throw new IllegalArgumentException("reportId is required");
        }

        Optional<Report> opt = cdwReportRepository.findById(reportId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Report not found: " + reportId);
        }

        Report report = opt.get();

        // Delete blob if present
        if (report.getBlobPath() != null && !report.getBlobPath().isBlank()) {
            try {
                blobStorageService.deleteFile(report.getBlobPath());
            } catch (Exception e) {
                log.warn("Failed to delete blob for report {}: {}", reportId, e.getMessage());
                // continue to delete DB record even if blob deletion fails
            }
        }

        // Delete report document
        cdwReportRepository.deleteById(reportId);
        log.info("Report {} deleted", reportId);
    }
}




