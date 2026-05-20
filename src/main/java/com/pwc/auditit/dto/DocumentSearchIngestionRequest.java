package com.pwc.auditit.dto;

import lombok.Data;

@Data
public class DocumentSearchIngestionRequest {

    private String path;

    private String indexName;

    private Integer batchSize;

    private Boolean deleteExisting;
}
