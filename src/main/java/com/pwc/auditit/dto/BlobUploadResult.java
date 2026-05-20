package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the result of uploading a file to Blob Storage
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlobUploadResult {
    
    private String blobPath;
    
    private String fileUrl;
    
    private String fileName;
}

