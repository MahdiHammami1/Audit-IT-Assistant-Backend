package com.pwc.auditit.client;

import com.pwc.auditit.config.FastApiProperties;
import com.pwc.auditit.dto.ChatbotRequest;
import com.pwc.auditit.dto.ChatbotResponse;
import com.pwc.auditit.dto.DocumentSearchIngestionRequest;
import com.pwc.auditit.dto.GeneratedReportsIndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Client for communicating with FastAPI service for CDW report generation
 */
@Slf4j
@Component
public class FastApiClient {

    private static final String FORMAT_PPTX = "pptx";
    
    private final WebClient webClient;
    private final FastApiProperties fastApiProperties;
    
    public FastApiClient(@Qualifier("fastApiWebClient") WebClient webClient,
                         FastApiProperties fastApiProperties) {
        this.webClient = webClient;
        this.fastApiProperties = fastApiProperties;
    }
    
    /**
     * Send an Excel file to FastAPI for CDW report generation
     * Returns the generated Word file as byte array
     * Adds missionId as header so FastAPI can associate the run if needed
     */
    public byte[] generateCdwReportFromExcel(MultipartFile excelFile, String missionId) {
        return generateCdwReportFromExcel(excelFile, missionId, "docx");
    }

    public byte[] generateCdwReportFromExcel(MultipartFile excelFile, String missionId, String outputFormat) {
        return generateCdwReportFileFromExcel(excelFile, missionId, outputFormat).content();
    }

    public ChatbotResponse askChatbot(ChatbotRequest request) {
        try {
            WebClient.RequestBodySpec webClientRequest = webClient.post()
                    .uri(fastApiProperties.getChatbotEndpoint())
                    .contentType(MediaType.APPLICATION_JSON);
            addInternalApiKey(webClientRequest);

            ChatbotResponse response = webClientRequest
                    .bodyValue(toFastApiChatbotPayload(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(errorBody -> Mono.error(new IllegalStateException(
                                    "FastAPI chatbot failed: HTTP "
                                            + clientResponse.statusCode().value()
                                            + formatFastApiError(errorBody)
                            ))))
                    .bodyToMono(ChatbotResponse.class)
                    .block();

            if (response == null) {
                throw new IllegalStateException("FastAPI chatbot returned an empty response");
            }
            return response;
        } catch (Exception e) {
            log.error("Unexpected error during FastAPI chatbot communication", e);
            throw new RuntimeException("Failed to ask FastAPI chatbot: " + describeException(e), e);
        }
    }

    public void indexGeneratedReportBlob(String blobPath,
                                         String reportId,
                                         String missionId,
                                         String fileName,
                                         String contentType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("blob_path", blobPath);
        payload.put("report_id", reportId);
        payload.put("mission_id", missionId);
        payload.put("file_name", fileName);
        payload.put("content_type", contentType);
        payload.put("index_name", "audit-platform-guide-index");

        WebClient.RequestBodySpec request = webClient.post()
                .uri(fastApiProperties.getGeneratedReportBlobIngestionEndpoint())
                .contentType(MediaType.APPLICATION_JSON);
        addInternalApiKey(request);

        request.bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> Mono.error(new IllegalStateException(
                                "FastAPI generated report indexing failed: HTTP "
                                        + response.statusCode().value()
                                        + formatFastApiError(errorBody)
                        ))))
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> getPlatformDocsStatus() {
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(fastApiProperties.getPlatformDocsStatusEndpoint());
        addInternalApiKey(request);

        return request.retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> Mono.error(new IllegalStateException(
                                "FastAPI platform docs status failed: HTTP "
                                        + response.statusCode().value()
                                        + formatFastApiError(errorBody)
                        ))))
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> indexPlatformChunks(DocumentSearchIngestionRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("path", request.getPath());
        payload.put("index_name", request.getIndexName());
        payload.put("batch_size", request.getBatchSize());
        payload.put("delete_existing", Boolean.TRUE.equals(request.getDeleteExisting()));

        return postJsonForMap(fastApiProperties.getPlatformDocsJsonlIngestionEndpoint(), payload);
    }

    public Map<String, Object> indexGeneratedReportsPrefix(GeneratedReportsIndexRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("prefix", request.getPrefix());
        payload.put("index_name", request.getIndexName());
        payload.put("max_files", request.getMaxFiles());

        return postJsonForMap(fastApiProperties.getGeneratedReportsBlobPrefixIngestionEndpoint(), payload);
    }

    /**
     * Send an Excel file to FastAPI for CDW report generation.
     * Chooses the Word or PowerPoint agent endpoint based on the requested output format.
     */
    public FastApiGeneratedFile generateCdwReportFileFromExcel(MultipartFile excelFile, String missionId, String outputFormat) {
        String normalizedFormat = normalizeOutputFormat(outputFormat);
        String endpoint = FORMAT_PPTX.equals(normalizedFormat)
                ? fastApiProperties.getCdwPptReportEndpoint()
                : fastApiProperties.getCdwReportEndpoint();
        String originalFileName = Objects.requireNonNullElse(excelFile.getOriginalFilename(), "cdw.xlsx");

        log.info(
                "Sending Excel file to FastAPI for CDW {} report generation: {} (missionId={})",
                normalizedFormat.toUpperCase(),
                originalFileName,
                missionId
        );

        try {
            byte[] excelBytes = excelFile.getBytes();
            ByteArrayResource fileResource = new ByteArrayResource(excelBytes) {
                @Override
                public String getFilename() {
                    return originalFileName;
                }
            };

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", fileResource)
                    .filename(originalFileName)
                    .contentType(resolveExcelMediaType(excelFile, originalFileName));

            WebClient.RequestBodySpec request = webClient.post()
                    .uri(endpoint)
                    .header("X-Mission-Id", missionId == null ? "" : missionId)
                    .contentType(MediaType.MULTIPART_FORM_DATA);

            addInternalApiKey(request);

            FastApiGeneratedFile generatedFile = request
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .exchangeToMono(response -> {
                        HttpStatusCode statusCode = response.statusCode();
                        if (statusCode.isError()) {
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(errorBody -> Mono.error(new IllegalStateException(
                                            "FastAPI " + normalizedFormat.toUpperCase() + " generation failed: HTTP "
                                                    + statusCode.value() + formatFastApiError(errorBody)
                                    )));
                        }
                        HttpHeaders headers = response.headers().asHttpHeaders();
                        String responseFileName = headers.getContentDisposition().getFilename();
                        String responseContentType = headers.getContentType() != null
                                ? headers.getContentType().toString()
                                : null;

                        return response.bodyToMono(byte[].class)
                                .map(content -> new FastApiGeneratedFile(
                                        content,
                                        responseFileName,
                                        responseContentType
                                ));
                    })
                    .block();

            if (generatedFile == null || generatedFile.content() == null || generatedFile.content().length == 0) {
                throw new IllegalStateException("FastAPI returned an empty " + normalizedFormat.toUpperCase() + " file");
            }

            return generatedFile;

        } catch (WebClientResponseException e) {
            log.error("FastAPI returned error: HTTP {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("FastAPI error: HTTP " + e.getRawStatusCode() + formatFastApiError(e.getResponseBodyAsString()), e);
        } catch (IllegalStateException e) {
            log.error("FastAPI CDW report generation failed: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("Unable to read uploaded Excel file before FastAPI call", e);
            throw new RuntimeException("Unable to read uploaded Excel file: " + safeMessage(e), e);
        } catch (Exception e) {
            log.error("Unexpected error during FastAPI communication", e);
            throw new RuntimeException("Unexpected error during FastAPI communication: " + describeException(e), e);
        }
    }

    private String normalizeOutputFormat(String outputFormat) {
        if (outputFormat == null || outputFormat.isBlank()) {
            return "docx";
        }

        String normalized = outputFormat.trim().toLowerCase();
        return normalized.equals("ppt") || normalized.equals(FORMAT_PPTX) || normalized.equals("powerpoint")
                ? FORMAT_PPTX
                : "docx";
    }

    private MediaType resolveExcelMediaType(MultipartFile excelFile, String fileName) {
        String contentType = excelFile.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            return MediaType.parseMediaType(contentType);
        }
        return fileName.toLowerCase().endsWith(".xls")
                ? MediaType.parseMediaType("application/vnd.ms-excel")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private Map<String, Object> postJsonForMap(String endpoint, Map<String, Object> payload) {
        WebClient.RequestBodySpec request = webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON);
        addInternalApiKey(request);

        return request.bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> Mono.error(new IllegalStateException(
                                "FastAPI document search request failed: HTTP "
                                        + response.statusCode().value()
                                        + formatFastApiError(errorBody)
                        ))))
                .bodyToMono(Map.class)
                .block();
    }

    private void addInternalApiKey(WebClient.RequestHeadersSpec<?> request) {
        if (fastApiProperties.getInternalApiKey() != null && !fastApiProperties.getInternalApiKey().isBlank()) {
            request.header("X-Internal-API-Key", fastApiProperties.getInternalApiKey());
        }
    }

    private Map<String, Object> toFastApiChatbotPayload(ChatbotRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", request.getMessage());
        payload.put("session_id", request.getSessionId());
        payload.put("top_k", request.getTopK() == null ? 5 : request.getTopK());
        payload.put("user_role", request.getUserRole());
        payload.put("filters", request.getFilters());
        return payload;
    }

    private String formatFastApiError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        return " - " + responseBody;
    }

    private String describeException(Exception e) {
        String message = e.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }

        String simpleName = e.getClass().getSimpleName();
        if (simpleName.contains("ReadTimeout")) {
            return "FastAPI did not return the generated report before the configured timeout. Increase FASTAPI_READ_TIMEOUT_MS or check the PPT agent logs.";
        }
        if (simpleName.contains("Timeout")) {
            return "FastAPI request timed out. Increase FASTAPI_READ_TIMEOUT_MS or check whether the FastAPI service is still processing.";
        }

        return simpleName;
    }

    private String safeMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    public record FastApiGeneratedFile(byte[] content, String fileName, String contentType) {
    }
}



