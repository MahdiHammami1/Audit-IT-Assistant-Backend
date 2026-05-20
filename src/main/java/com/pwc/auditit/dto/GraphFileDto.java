package com.pwc.auditit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphFileDto {

    private String id;
    private String name;
    private String webUrl;
    private Long size;
    private String lastModifiedDateTime;
}
