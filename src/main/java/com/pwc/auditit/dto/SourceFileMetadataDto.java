package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO containing metadata of the source file
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SourceFileMetadataDto {
    
    private String sourceFileName;
    
    private String sourceFileHash;
    
    private Long sourceFileSize;
    
    private String contentType;
}

