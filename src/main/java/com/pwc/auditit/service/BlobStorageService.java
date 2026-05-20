package com.pwc.auditit.service;

import com.pwc.auditit.dto.BlobUploadResult;

/**
 * Interface for Blob Storage operations
 */
public interface BlobStorageService {
    
    /**
     * Upload a file to Blob Storage
     * 
     * @param fileData the file content as byte array
     * @param fileName the original file name
     * @param missionId the mission ID for organizing files
     * @return BlobUploadResult containing blob path and URL
     */
    BlobUploadResult uploadFile(byte[] fileData, String fileName, String missionId);

    /**
     * Upload a file to Blob Storage with an explicit content type.
     *
     * @param fileData the file content as byte array
     * @param fileName the original file name
     * @param missionId the mission ID for organizing files
     * @param contentType the MIME type to store with the blob
     * @return BlobUploadResult containing blob path and URL
     */
    BlobUploadResult uploadFile(byte[] fileData, String fileName, String missionId, String contentType);

    /**
     * Download a file from Blob Storage
     *
     * @param blobPath the blob path stored in MongoDB
     * @return file content as byte array
     */
    byte[] downloadFile(String blobPath);

    /**
     * Delete a file from Blob Storage
     *
     * @param blobPath the blob path stored in MongoDB
     */
    void deleteFile(String blobPath);
}
