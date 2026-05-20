package com.pwc.auditit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Azure Blob Storage integration
 */
@Component
@ConfigurationProperties(prefix = "azure.blob-storage")
@Getter
@Setter
public class BlobStorageProperties {
    
    private String connectionString;
    
    private String containerName;
    
    private String reportsPath = "missions/{missionId}/reports";
}

