package com.pwc.auditit.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.pwc.auditit.config.BlobStorageProperties;
import com.pwc.auditit.dto.BlobUploadResult;
import com.pwc.auditit.service.BlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Azure Blob Storage implementation for file storage
 */
@Slf4j
@Service
public class AzureBlobStorageService implements BlobStorageService {
    
    private final BlobStorageProperties blobStorageProperties;
    private final BlobServiceClient blobServiceClient;
    
    public AzureBlobStorageService(BlobStorageProperties blobStorageProperties) {
        this.blobStorageProperties = blobStorageProperties;
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(blobStorageProperties.getConnectionString())
                .buildClient();
    }
    
    /**
     * Upload a file to Azure Blob Storage
     * File path format: missions/{missionId}/reports/{uuid}_{filename}
     */
    @Override
    public BlobUploadResult uploadFile(byte[] fileData, String fileName, String missionId) {
        return uploadFile(fileData, fileName, missionId, "application/octet-stream");
    }

    /**
     * Upload a file to Azure Blob Storage with its MIME type.
     * File path format: missions/{missionId}/reports/{uuid}_{filename}
     */
    @Override
    public BlobUploadResult uploadFile(byte[] fileData, String fileName, String missionId, String contentType) {
        log.info("Uploading file to Blob Storage: {} for mission: {}", fileName, missionId);

        try {
            String blobPath = buildBlobPath(missionId, fileName);
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
                    blobStorageProperties.getContainerName()
            );
            containerClient.createIfNotExists();
            
            BlobClient blobClient = containerClient.getBlobClient(blobPath);
            
            String resolvedContentType = contentType == null || contentType.isBlank()
                    ? "application/octet-stream"
                    : contentType;
            blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(resolvedContentType));
            
            String fileUrl = blobClient.getBlobUrl();
            
            log.info("File uploaded successfully to: {}", blobPath);
            
            return new BlobUploadResult(blobPath, fileUrl, fileName);
            
        } catch (Exception e) {
            log.error("Error uploading file to Blob Storage: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to Blob Storage: " + fileName, e);
        }
    }

    /**
     * Download a file from Azure Blob Storage
     */
    @Override
    public byte[] downloadFile(String blobPath) {
        log.info("Downloading file from Blob Storage: {}", blobPath);

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
                    blobStorageProperties.getContainerName()
            );

            BlobClient blobClient = containerClient.getBlobClient(blobPath);

            if (!blobClient.exists()) {
                log.error("Blob not found at path: {}", blobPath);
                throw new RuntimeException("File not found in storage: " + blobPath);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blobClient.download(out);
            byte[] data = out.toByteArray();
            log.info("File downloaded successfully from: {} ({} bytes)", blobPath, data.length);
            return data;

        } catch (Exception e) {
            log.error("Error downloading file from Blob Storage: {}", blobPath, e);
            throw new RuntimeException("Failed to download file: " + blobPath, e);
        }
    }

    /**
     * Delete a file from Azure Blob Storage
     */
    @Override
    public void deleteFile(String blobPath) {
        log.info("Deleting blob from storage: {}", blobPath);
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
                    blobStorageProperties.getContainerName()
            );

            BlobClient blobClient = containerClient.getBlobClient(blobPath);

            if (!blobClient.exists()) {
                log.warn("Blob not found (nothing to delete): {}", blobPath);
                return;
            }

            blobClient.delete();
            log.info("Blob deleted: {}", blobPath);
        } catch (Exception e) {
            log.error("Error deleting blob: {}", blobPath, e);
            throw new RuntimeException("Failed to delete blob: " + blobPath, e);
        }
    }

    /**
     * Build the blob path for a file
     * Format: missions/{missionId}/reports/{uuid}_{filename}
     */
    private String buildBlobPath(String missionId, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return String.format("missions/%s/reports/%s_%s", missionId, uuid, fileName);
    }
}

