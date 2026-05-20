package com.pwc.auditit.controller;

import com.pwc.auditit.client.FastApiClient;
import com.pwc.auditit.config.DocumentSearchProperties;
import com.pwc.auditit.dto.DocumentSearchIngestionRequest;
import com.pwc.auditit.dto.GeneratedReportsIndexRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/document-search")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final FastApiClient fastApiClient;
    private final DocumentSearchProperties documentSearchProperties;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        try {
            Map<String, Object> fastApiStatus = fastApiClient.getPlatformDocsStatus();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("springConfiguredEndpoint", documentSearchProperties.getAzureSearch().getEndpoint());
            response.put("springConfiguredIndexName", documentSearchProperties.getAzureSearch().getIndexName());
            response.put("fastApi", fastApiStatus);
            return ResponseEntity.ok(ApiResponse.ok("Document search status retrieved", response));
        } catch (Exception e) {
            log.error("Unable to retrieve document search status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve document search status: " + e.getMessage()));
        }
    }

    @PostMapping("/platform-chunks/index")
    public ResponseEntity<ApiResponse<Map<String, Object>>> indexPlatformChunks(
            @RequestBody(required = false) DocumentSearchIngestionRequest request
    ) {
        try {
            DocumentSearchIngestionRequest resolved = resolvePlatformChunksRequest(request);
            Map<String, Object> result = fastApiClient.indexPlatformChunks(resolved);
            return ResponseEntity.ok(ApiResponse.ok("Platform RAG chunks indexed", result));
        } catch (Exception e) {
            log.error("Unable to index platform RAG chunks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to index platform RAG chunks: " + e.getMessage()));
        }
    }

    @PostMapping("/generated-reports/index")
    public ResponseEntity<ApiResponse<Map<String, Object>>> indexGeneratedReports(
            @RequestBody(required = false) GeneratedReportsIndexRequest request
    ) {
        try {
            GeneratedReportsIndexRequest resolved = resolveGeneratedReportsRequest(request);
            Map<String, Object> result = fastApiClient.indexGeneratedReportsPrefix(resolved);
            return ResponseEntity.ok(ApiResponse.ok("Generated reports indexed", result));
        } catch (Exception e) {
            log.error("Unable to index generated reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to index generated reports: " + e.getMessage()));
        }
    }

    private DocumentSearchIngestionRequest resolvePlatformChunksRequest(DocumentSearchIngestionRequest request) {
        DocumentSearchIngestionRequest resolved = request != null ? request : new DocumentSearchIngestionRequest();
        if (resolved.getPath() == null || resolved.getPath().isBlank()) {
            resolved.setPath(documentSearchProperties.getPlatformChunks().getPath());
        }
        if (resolved.getIndexName() == null || resolved.getIndexName().isBlank()) {
            resolved.setIndexName(documentSearchProperties.getAzureSearch().getIndexName());
        }
        if (resolved.getBatchSize() == null) {
            resolved.setBatchSize(documentSearchProperties.getPlatformChunks().getBatchSize());
        }
        if (resolved.getDeleteExisting() == null) {
            resolved.setDeleteExisting(false);
        }
        return resolved;
    }

    private GeneratedReportsIndexRequest resolveGeneratedReportsRequest(GeneratedReportsIndexRequest request) {
        GeneratedReportsIndexRequest resolved = request != null ? request : new GeneratedReportsIndexRequest();
        if (resolved.getPrefix() == null || resolved.getPrefix().isBlank()) {
            resolved.setPrefix(documentSearchProperties.getGeneratedReports().getBlobPrefix());
        }
        if (resolved.getIndexName() == null || resolved.getIndexName().isBlank()) {
            resolved.setIndexName(documentSearchProperties.getAzureSearch().getIndexName());
        }
        if (resolved.getMaxFiles() == null) {
            resolved.setMaxFiles(documentSearchProperties.getGeneratedReports().getMaxFiles());
        }
        return resolved;
    }
}
