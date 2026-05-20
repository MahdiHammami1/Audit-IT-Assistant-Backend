package com.pwc.auditit.dto;

import lombok.Data;

@Data
public class GeneratedReportsIndexRequest {

    private String prefix;

    private String indexName;

    private Integer maxFiles;
}
