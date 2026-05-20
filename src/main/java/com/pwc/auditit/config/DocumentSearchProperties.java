package com.pwc.auditit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "document-search")
public class DocumentSearchProperties {

    private AzureSearch azureSearch = new AzureSearch();

    private PlatformChunks platformChunks = new PlatformChunks();

    private GeneratedReports generatedReports = new GeneratedReports();

    @Getter
    @Setter
    public static class AzureSearch {
        private String endpoint;
        private String indexName = "audit-platform-guide-index";
    }

    @Getter
    @Setter
    public static class PlatformChunks {
        private String path = "docs/rag-output/rag_chunks.jsonl";
        private int batchSize = 50;
    }

    @Getter
    @Setter
    public static class GeneratedReports {
        private String blobPrefix = "missions/";
        private int maxFiles = 50;
    }
}
