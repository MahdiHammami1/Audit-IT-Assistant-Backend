package com.pwc.auditit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for FastAPI integration
 */
@Component
@ConfigurationProperties(prefix = "fastapi")
@Getter
@Setter
public class FastApiProperties {
    
    private String baseUrl;
    
    private String internalApiKey;
    
    private String cdwReportEndpoint = "/agents/cdw-report";

    private String cdwPptReportEndpoint = "/agents/cdw-ppt-report";

    private String chatbotEndpoint = "/agents/chatbot";

    private String generatedReportBlobIngestionEndpoint = "/ingestion/generated-reports/blob";

    private String generatedReportsBlobPrefixIngestionEndpoint = "/ingestion/generated-reports/blob-prefix";

    private String platformDocsJsonlIngestionEndpoint = "/ingestion/platform-docs/jsonl";

    private String platformDocsStatusEndpoint = "/ingestion/platform-docs/status";
    
    private int connectTimeoutMs = 30000;
    
    private int readTimeoutMs = 900000;

    private int maxInMemorySizeBytes = 104857600;
}

