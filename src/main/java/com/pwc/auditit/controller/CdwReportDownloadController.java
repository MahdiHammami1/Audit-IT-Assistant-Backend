    package com.pwc.auditit.controller;

    import com.pwc.auditit.dto.response.ApiResponse;
    import com.pwc.auditit.model.Report;
    import com.pwc.auditit.repository.CdwReportRepository;
    import com.pwc.auditit.service.BlobStorageService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.ContentDisposition;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    /**
     * Controller for secure CDW Report downloads
     */
    @Slf4j
    @RestController
    @RequestMapping("/cdw-reports")
    @RequiredArgsConstructor
    @Tag(name = "CDW Reports", description = "CDW report download")
    public class CdwReportDownloadController {

        private static final String WORD_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        private static final String POWERPOINT_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        private final CdwReportRepository cdwReportRepository;
        private final BlobStorageService blobStorageService;

        @GetMapping("/download/{reportId}")
        @Operation(summary = "Download CDW report file")
        public ResponseEntity<?> downloadReport(@PathVariable String reportId) {
            log.info("Received download request for report: {}", reportId);

            try {
                Report report = cdwReportRepository.findById(reportId)
                        .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

                if (report.getBlobPath() == null) {
                    log.warn("Report {} has no blobPath", reportId);
                    return ResponseEntity.badRequest().body(ApiResponse.error("No file associated with report"));
                }

                byte[] fileData = blobStorageService.downloadFile(report.getBlobPath());
                String fileName = resolveDownloadFileName(report);
                MediaType mediaType = MediaType.parseMediaType(resolveContentType(report, fileName));

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                                .filename(fileName)
                                .build()
                                .toString())
                        .contentType(mediaType)
                        .contentLength(fileData.length)
                        .body(fileData);

            } catch (RuntimeException e) {
                log.error("Error downloading report: {}", reportId, e);
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            } catch (Exception e) {
                log.error("Unexpected error downloading report: {}", reportId, e);
                return ResponseEntity.status(500).body(ApiResponse.error("Failed to download report"));
            }
        }

        private String resolveDownloadFileName(Report report) {
            if (report.getGeneratedFileName() != null && !report.getGeneratedFileName().isBlank()) {
                return report.getGeneratedFileName();
            }
            String format = report.getFormat() != null && !report.getFormat().isBlank() ? report.getFormat() : "docx";
            return "cdw-report." + format;
        }

        private String resolveContentType(Report report, String fileName) {
            if (report.getContentType() != null && !report.getContentType().isBlank()) {
                return report.getContentType();
            }
            if (fileName != null && fileName.toLowerCase().endsWith(".pptx")) {
                return POWERPOINT_CONTENT_TYPE;
            }
            if ("pptx".equalsIgnoreCase(report.getFormat())) {
                return POWERPOINT_CONTENT_TYPE;
            }
            return WORD_CONTENT_TYPE;
        }
    }
